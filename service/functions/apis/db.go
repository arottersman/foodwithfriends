package main

import (
	"bytes"
	"database/sql"
	"errors"
	"fmt"
	_ "github.com/lib/pq"
	"os"
	"regexp"
	"strings"
	"time"
)

func Connect() (*sql.DB, error) {
	user := os.Getenv("PG_USER")

	password := os.Getenv("PG_PASSWORD")
	dbname := os.Getenv("PG_DBNAME")
	host := os.Getenv("PG_HOST")
	connectionString := fmt.Sprintf(
		"user=%s dbname=%s password=%s host=%s sslmode=disable",
		user, dbname, password, host)

	return sql.Open("postgres", connectionString)
}

func dbNullStringToArray(s sql.NullString) []string {
	if s.Valid {
		return strings.Split(s.String, "+")
	} else {
		return make([]string, 0)
	}
}

func CreateUser(db *sql.DB, user User) (int64, error) {
	dietaryRestrictionsString := strings.Join(user.DietaryRestrictions,
		"+")
	var userId int64
	err := db.QueryRow(
		`INSERT INTO users (
                     name,
                     email,
                     dietary_restrictions,
                     auth0_id
                 ) VALUES ($1, $2, $3, $4)
                 RETURNING user_id`,
		user.Name,
		user.Email,
		dietaryRestrictionsString,
		user.Auth0Id).Scan(&userId)

	if err != nil {
		return 0, err
	}

	return userId, nil
}

func GetUserByAuth0Id(db *sql.DB, auth0Id string) (User, error) {
	row := db.QueryRow(`SELECT users.user_id,
                               users.name,
                               users.email,
                               users.dietary_restrictions,
                               users.auth0_id,
                               host_users.host_id
                            FROM users
                            LEFT JOIN host_users
                            ON users.user_id = host_users.user_id
                            WHERE auth0_id = $1
                            `, auth0Id)

	var (
		user_id              int64
		name                 string
		email                string
		dietary_restrictions sql.NullString
		auth0_id             string
		host_id              sql.NullInt64
	)
	err := row.Scan(&user_id, &name, &email, &dietary_restrictions, &auth0_id, &host_id)

	if err != nil {
		return User{}, err
	}

	dietaryRestrictionsArray := dbNullStringToArray(dietary_restrictions)

	return User{
		UserId:              user_id,
		Name:                name,
		Email:               email,
		DietaryRestrictions: dietaryRestrictionsArray,
		Auth0Id:             auth0_id,
		HostId:              host_id.Int64,
	}, nil
}

func UpdateUser(db *sql.DB, user User) (User, error) {
	var colsToUpdate []string
	var updates []interface{}

	if user.Name != "" {
		colsToUpdate = append(colsToUpdate, "name")
		updates = append(updates, user.Name)
	}
	if user.Email != "" {
		colsToUpdate = append(colsToUpdate, "email")
		updates = append(updates, user.Email)
	}
	if user.DietaryRestrictions != nil {
		colsToUpdate = append(colsToUpdate, "dietary_restrictions")
		updates = append(updates, strings.Join(user.DietaryRestrictions, "+"))
	}

	colsToUpdateString := strings.Join(colsToUpdate, ", ")
	var buffer bytes.Buffer
	for i := 0; i < len(colsToUpdate)-1; i++ {
		buffer.WriteString(fmt.Sprintf("$%d, ", i+1))
	}
	buffer.WriteString(fmt.Sprintf("$%d", len(colsToUpdate)))
	paramString := buffer.String()

	query := fmt.Sprintf(`UPDATE users SET (%s) = (%s)
                              WHERE auth0_id = '%s'
                              RETURNING name, email,
                              dietary_restrictions, auth0_id`,
		colsToUpdateString, paramString, user.Auth0Id)

	row := db.QueryRow(query, updates...)

	var (
		name                 string
		email                string
		dietary_restrictions sql.NullString
		auth0_id             string
	)
	scanErr := row.Scan(&name, &email, &dietary_restrictions, &auth0_id)
	if scanErr != nil {
		return User{}, scanErr
	}

	dietaryRestrictionsArray := dbNullStringToArray(dietary_restrictions)

	return User{
		Name:                name,
		Email:               email,
		DietaryRestrictions: dietaryRestrictionsArray,
		Auth0Id:             auth0_id,
	}, nil
}

func CreateHost(db *sql.DB, host Host) (int64, error) {
	var hostId int64
	err := db.QueryRow(
		`INSERT INTO hosts (
                         address,
                         city,
                         state,
                         zipcode,
                         max_occupancy
                     ) VALUES ($1, $2, $3, $4, $5)
                     RETURNING host_id`,
		host.Address,
		host.City,
		host.State,
		host.Zipcode,
		host.MaxOccupancy).Scan(&hostId)

	if err != nil {
		return 0, err
	}

	var buffer bytes.Buffer
	var insertValues []interface{}

	for i := 0; i < len(host.Users)-1; i++ {
		insertValues = append(insertValues, hostId)

		insertValues = append(insertValues, host.Users[i].UserId)
		var argumentCount = (i+1)*2 - 1
		buffer.WriteString(fmt.Sprintf("($%d, $%d), ", argumentCount,
			argumentCount+1))
	}
	insertValues = append(insertValues, hostId)
	insertValues = append(insertValues, host.Users[len(host.Users)-1].UserId)
	var argumentCount = len(host.Users)*2 - 1
	buffer.WriteString(fmt.Sprintf("($%d, $%d) ", argumentCount,
		argumentCount+1))
	paramString := buffer.String()

	insertHostUserQuery := fmt.Sprintf(
		`INSERT INTO host_users (
                    host_id,
                    user_id
                 ) VALUES %s`, paramString)
	_, insertHostUserErr := db.Exec(insertHostUserQuery,
		insertValues...)

	if insertHostUserErr != nil {
		return 0, insertHostUserErr
	}

	return hostId, nil
}

func GetUsersForHost(db *sql.DB, hostId int64) (Users, error) {
	rows, err := db.Query(`SELECT users.user_id,
                                users.name,
                                users.email,
                                users.dietary_restrictions,
                                users.auth0_id
                            FROM users, host_users
                            WHERE host_users.host_id = $1
                            AND host_users.user_id = users.user_id`, hostId)
	if err != nil {
		return Users{}, err
	}
	defer rows.Close()

	users := Users{}
	for rows.Next() {
		var (
			user_id              int64
			name                 string
			email                string
			dietary_restrictions sql.NullString
			auth0_id             string
		)
		scanErr := rows.Scan(&user_id, &name, &email,
			&dietary_restrictions, &auth0_id)

		dietaryRestrictionsArray := dbNullStringToArray(dietary_restrictions)

		if scanErr != nil {
			return Users{}, scanErr
		}

		users = append(users, User{
			UserId:              user_id,
			Name:                name,
			Email:               email,
			DietaryRestrictions: dietaryRestrictionsArray,
			Auth0Id:             auth0_id,
			HostId:              hostId,
		})
	}
	return users, nil
}

func GetHost(db *sql.DB, hostId int64) (Host, error) {
	row := db.QueryRow(`SELECT host_id, address, city,
                            state, zipcode, max_occupancy
                            FROM hosts WHERE host_id = $1`, hostId)

	var (
		host_id       int64
		address       string
		city          string
		state         string
		zipcode       string
		max_occupancy int64
	)
	scanErr := row.Scan(&host_id, &address, &city,
		&state, &zipcode, &max_occupancy)
	if scanErr != nil {
		return Host{}, scanErr
	}

	users, getUserErr := GetUsersForHost(db, hostId)

	if getUserErr != nil {
		return Host{}, getUserErr
	}

	return Host{
		HostId:       host_id,
		Address:      address,
		City:         city,
		State:        state,
		Zipcode:      zipcode,
		MaxOccupancy: max_occupancy,
		Users:        users,
	}, nil
}

func ReadHostsFromQueryResults(db *sql.DB, rows *sql.Rows) (Hosts, error) {
	hosts := Hosts{}
	for rows.Next() {
		var (
			hostId       int64
			address      string
			city         string
			state        string
			zipcode      string
			maxOccupancy int64
		)

		if err := rows.Scan(
			&hostId,
			&address,
			&city,
			&state,
			&zipcode,
			&maxOccupancy,
		); err != nil {
			return Hosts{}, err
		}

		users, err := GetUsersForHost(db, hostId)
		if err != nil {
			return Hosts{}, err
		}
		if len(users) <= 0 {
			return Hosts{}, errors.New("Didn't find any users for host")
		}

		hosts = append(hosts, Host{
			HostId:       hostId,
			Address:      address,
			City:         city,
			State:        state,
			Zipcode:      zipcode,
			MaxOccupancy: maxOccupancy,
			Users:        users,
		})
	}

	if err := rows.Err(); err != nil {
		return Hosts{}, err
	}

	return hosts, nil
}

func GetHostsByAddress(db *sql.DB, address string) (Hosts, error) {
	numRegex := regexp.MustCompile("[0-9]+")
	addressNums := strings.Join(numRegex.FindAllString(address, -1), "|")
	rows, err := db.Query(
		`SELECT hosts.host_id,
                hosts.address,
                hosts.city,
                hosts.state,
                hosts.zipcode,
                hosts.max_occupancy
              FROM hosts
              WHERE hosts.address SIMILAR TO '%(' || $1 || ')%'
        `, addressNums)
	if err != nil {
		return Hosts{}, err
	}

	return ReadHostsFromQueryResults(db, rows)
}

func GetLeastRecentHosts(db *sql.DB, numHosts int) (Hosts, error) {
	rows, err := db.Query(
		`SELECT h.* FROM (
                      (SELECT hosts.host_id,
                          hosts.address,
                          hosts.city,
                          hosts.state,
                          hosts.zipcode,
                          hosts.max_occupancy
                      FROM hosts
                      LEFT JOIN events
                      ON events.host_id = hosts.host_id
                      WHERE events.host_id IS NULL)
                   UNION
                     (SELECT hosts.host_id,
                          hosts.address,
                          hosts.city,
                          hosts.state,
                          hosts.zipcode,
                          hosts.max_occupancy
                     FROM hosts, events, event_creation_invites
                     WHERE events.host_id = hosts.host_id
                     AND event_creation_invites.host_id = hosts.host_id
                     AND event_creation_invites.status != 'pending'
                     ORDER BY events.created_at)
                 ) AS h
                 LIMIT $1`, numHosts)

	if err != nil {
		return Hosts{}, err
	}

	return ReadHostsFromQueryResults(db, rows)
}

func AddUserToHost(db *sql.DB, hostId int64, userId int64) (Host, error) {
	_, addUserErr := db.Exec(`INSERT INTO host_users
                                  (host_id, user_id)
                                  VALUES ($1, $2)`,
		hostId, userId)

	if addUserErr != nil {
		return Host{}, addUserErr
	}

	host, getHostErr := GetHost(db, hostId)

	if getHostErr != nil {
		return Host{}, getHostErr
	}

	return host, nil
}

func UpdateHost(db *sql.DB, host Host) (Host, error) {
	var colsToUpdate []string
	var updates []interface{}

	if host.Address != "" {
		colsToUpdate = append(colsToUpdate, "address")
		updates = append(updates, host.Address)
	}
	if host.City != "" {
		colsToUpdate = append(colsToUpdate, "city")
		updates = append(updates, host.City)
	}
	if host.State != "" {
		colsToUpdate = append(colsToUpdate, "state")
		updates = append(updates, host.State)
	}
	if host.Zipcode != "" {
		colsToUpdate = append(colsToUpdate, "zipcode")
		updates = append(updates, host.Zipcode)
	}
	if host.MaxOccupancy != 0 {
		colsToUpdate = append(colsToUpdate, "max_occupancy")
		updates = append(updates, host.MaxOccupancy)
	}

	colsToUpdateString := strings.Join(colsToUpdate, ", ")
	var buffer bytes.Buffer
	for i := 0; i < len(colsToUpdate)-1; i++ {
		buffer.WriteString(fmt.Sprintf("$%d, ", i+1))
	}
	buffer.WriteString(fmt.Sprintf("$%d", len(colsToUpdate)))
	paramString := buffer.String()

	query := fmt.Sprintf(`UPDATE hosts SET (%s) = (%s)
                              WHERE host_id = '%d'
                              RETURNING
                                 address,
                                 city,
                                 state,
                                 zipcode,
                                 max_occupancy`,
		colsToUpdateString, paramString, host.HostId)

	row := db.QueryRow(query, updates...)

	var (
		address       string
		city          string
		state         string
		zipcode       string
		max_occupancy int64
	)

	err := row.Scan(&address, &city, &state, &zipcode, &max_occupancy)
	if err != nil {
		return Host{}, err
	}

	users, getUsersErr := GetUsersForHost(db, host.HostId)

	if getUsersErr != nil {
		return Host{}, getUsersErr
	}

	return Host{
		HostId:       host.HostId,
		Address:      address,
		City:         city,
		State:        state,
		Zipcode:      zipcode,
		MaxOccupancy: max_occupancy,
		Users:        users,
	}, nil
}

func CreateEvent(db *sql.DB, event Event) (int64, error) {
	var eventId int64
	err := db.QueryRow(
		`INSERT INTO events (
                         title,
                         happening_at,
                         host_id
                     ) VALUES ($1, $2, $3, $4)
                     RETURNING event_id`,
		event.Title,
		event.HappeningAt,
		event.Host.HostId).Scan(&eventId)

	if err != nil {
		return 0, err
	}

	return eventId, nil
}

func GetEvent(db *sql.DB, eventId int64) (Event, error) {
	db, err := Connect()

	if err != nil {
		return Event{}, err
	}
	row := db.QueryRow(`SELECT event_id, title,
                            happening_at, host_id
                            FROM events WHERE event_id = $1`, eventId)

	var (
		event_id    int64
		title       string
		happeningAt time.Time
		hostId      int64
	)
	scanErr := row.Scan(&event_id, &title,
		&happeningAt, &hostId)

	if scanErr != nil {
		return Event{}, scanErr
	}

	host, getHostErr := GetHost(db, hostId)

	if getHostErr != nil {
		return Event{}, getHostErr
	}

	users, getUsersErr := GetUsersForEvent(db, eventId)

	if getUsersErr != nil {
		return Event{}, getUsersErr
	}

	return Event{
		EventId:      eventId,
		Title:        title,
		HappeningAt:  happeningAt,
		Host:         host,
		Participants: users,
	}, nil
}

func AddUserToEvent(db *sql.DB, eventId int64, userId int64) (Event, error) {
	_, addUserErr := db.Exec(`INSERT INTO event_users (
                                   event_id,
                                   user_id,
                                   assigned_dish
                                  )
                                  VALUES ($1, $2,
                                       next_dish((SELECT eu2.assigned_dish
                                        FROM event_users eu2
                                        WHERE created_at = (
                                           SELECT MAX(created_at)
                                           FROM event_users eu3
                                           WHERE eu3.event_id = $1)
                                        )::dish))`,
		eventId, userId)

	if addUserErr != nil {
		return Event{}, addUserErr
	}

	event, getEventErr := GetEvent(db, eventId)

	if getEventErr != nil {
		return Event{}, getEventErr
	}

	return event, nil
}

func GetUsersForEvent(db *sql.DB, eventId int64) (Users, error) {
	rows, queryErr := db.Query(`SELECT DISTINCT users.user_id,
                                users.name,
                                users.email,
                                users.dietary_restrictions,
                                users.auth0_id,
                                event_users.assigned_dish,
                                event_users.bringing
                            FROM users, event_users
                            WHERE event_users.event_id = $1
                            AND event_users.user_id = users.user_id`, eventId)
	if queryErr != nil {
		return Users{}, queryErr
	}
	defer rows.Close()

	users := Users{}
	for rows.Next() {
		var (
			user_id              int64
			name                 string
			email                string
			dietary_restrictions sql.NullString
			auth0_id             string
			assigned_dish        sql.NullString
			bringing             sql.NullString
		)
		scanErr := rows.Scan(&user_id, &name, &email,
			&dietary_restrictions, &auth0_id,
			&assigned_dish, &bringing)

		dietaryRestrictionsArray :=
			dbNullStringToArray(dietary_restrictions)

		if scanErr != nil {
			return Users{}, scanErr
		}

		users = append(users, User{
			UserId:              user_id,
			Name:                name,
			Email:               email,
			DietaryRestrictions: dietaryRestrictionsArray,
			Auth0Id:             auth0_id,
			AssignedDish:        NullStringToString(assigned_dish),
			Bringing:            NullStringToString(bringing),
		})
	}
	return users, nil
}

func NullStringToString(nullStr sql.NullString) string {
	if nullStr.Valid {
		return nullStr.String
	}
	return ""
}

func UpdateEvent(db *sql.DB, event Event) (Event, error) {
	var colsToUpdate []string
	var updates []interface{}
	if event.Title != "" {
		colsToUpdate = append(colsToUpdate, "title")
		updates = append(updates, event.Title)
	}
	if !event.HappeningAt.IsZero() {
		colsToUpdate = append(colsToUpdate, "happening_at")
		updates = append(updates, event.HappeningAt)
	}
	if event.Host.HostId != 0 {
		colsToUpdate = append(colsToUpdate, "host_id")
		updates = append(updates, event.Host.HostId)
	}

	colsToUpdateString := strings.Join(colsToUpdate, ", ")
	var buffer bytes.Buffer
	for i := 0; i < len(colsToUpdate)-1; i++ {
		buffer.WriteString(fmt.Sprintf("$%d, ", i+1))
	}
	buffer.WriteString(fmt.Sprintf("$%d", len(colsToUpdate)))
	paramString := buffer.String()

	query := fmt.Sprintf(`UPDATE events SET (%s) = (%s)
                          WHERE event_id = '%d'
                          RETURNING
                               title,
                               happening_at,
                               host_id`,
		colsToUpdateString, paramString, event.EventId)

	row := db.QueryRow(query, updates...)

	var (
		title       string
		happeningAt time.Time
		hostId      int64
	)

	err := row.Scan(&title, &happeningAt, &hostId)
	if err != nil {
		return Event{}, err
	}

	host, err := GetHost(db, hostId)
	if err != nil {
		return Event{}, err
	}

	participants, err := GetUsersForEvent(db, event.EventId)
	if err != nil {
		return Event{}, err
	}

	return Event{
		EventId:      event.EventId,
		Title:        title,
		HappeningAt:  happeningAt,
		Participants: participants,
		Host:         host,
	}, nil
}

func ReadEventsFromQueryResults(db *sql.DB, rows *sql.Rows) (Events, error) {
	events := Events{}
	for rows.Next() {
		var (
			eventId     int64
			title       string
			happeningAt time.Time
			hostId      int64
		)
		if scanErr := rows.Scan(
			&eventId,
			&title,
			&happeningAt,
			&hostId); scanErr != nil {
			return Events{}, scanErr
		}
		host, getHostErr := GetHost(db, hostId)

		if getHostErr != nil {
			return Events{}, getHostErr
		}

		participants, getUsersErr := GetUsersForEvent(db, eventId)

		if getUsersErr != nil {
			return Events{}, getUsersErr
		}

		events = append(events, Event{
			EventId:      eventId,
			Title:        title,
			HappeningAt:  happeningAt,
			Participants: participants,
			Host:         host,
		})
	}

	if err := rows.Err(); err != nil {
		return Events{}, err
	}

	return events, nil
}

func GetPastEventsForUser(db *sql.DB, userId int64) (Events, error) {
	rows, err := db.Query(
		`SELECT * FROM
                 ((SELECT
                        events.event_id,
                        events.title,
                        events.happening_at,
                        events.host_id
                 FROM events, event_users
                 WHERE event_users.user_id = $1
	         AND event_users.event_id = events.event_id
                )
		UNION
		(SELECT
		        events.event_id,
		        events.title,
		        events.happening_at,
		        events.host_id
		 FROM events, host_users
		 WHERE host_users.host_id = events.host_id
		 AND host_users.user_id = $1
                )) as result
                 WHERE result.happening_at < current_timestamp
                 ORDER BY result.happening_at DESC`, userId)

	if err != nil {
		return Events{}, err
	}

	return ReadEventsFromQueryResults(db, rows)
}

func GetCurrentEvents(db *sql.DB) (Events, error) {
	rows, queryErr := db.Query(
		`SELECT events.event_id,
                        events.title,
                        events.happening_at,
                        events.host_id
                 FROM events
                 WHERE events.happening_at >= current_timestamp`)

	if queryErr != nil {
		return Events{}, queryErr
	}

	return ReadEventsFromQueryResults(db, rows)
}

func GetPendingHosts(db *sql.DB) (Hosts, error) {
	rows, err := db.Query(
		`SELECT hosts.host_id,
                hosts.address,
                hosts.city,
                hosts.state,
                hosts.zipcode,
                hosts.max_occupancy
         FROM hosts, event_creation_invites
         WHERE event_creation_invites.status = 'pending'
         AND hosts.host_id = event_creation_invites.host_id`)

	if err != nil {
		return Hosts{}, err
	}

	return ReadHostsFromQueryResults(db, rows)
}

func AddHostInvitations(db *sql.DB, hosts Hosts) error {
	var buffer bytes.Buffer
	var insertValues []interface{}

	for i, host := range hosts {
		insertValues = append(insertValues, host.HostId)
		insertValues = append(insertValues, PENDING)

		var argumentCount = (i+1)*2 - 1

		var valueStr string
		if valueStr = "($%d, $%d), "; i == len(hosts)-1 {
			valueStr = "($%d, $%d) "
		}

		buffer.WriteString(fmt.Sprintf(valueStr, argumentCount,
			argumentCount+1))
	}

	paramStr := buffer.String()

	query := fmt.Sprintf(
		`INSERT INTO event_creation_invites (
                        host_id,
                        status
                     ) VALUES %s`, paramStr)
	_, err := db.Exec(query,
		insertValues...)

	if err != nil {
		return err
	}
	return nil
}

func UpdateHostInvitation(db *sql.DB, hostId int64, status string) error {
	_, err := db.Exec(`UPDATE event_creation_invites SET (
                                 status
                              ) = (
                                 $1
                              )
                              WHERE host_id = $2
                              AND status != 'complete'
                              AND status != 'pass'`,
		status, hostId)
	if err != nil {
		return err
	}
	return nil
}

func ExpireEventInvitations(db *sql.DB) (Hosts, error) {
	rows, err := db.Query(`UPDATE event_creation_invites SET (
                                 status
                              ) = (
                                 'complete'
                              )
                              WHERE status = 'event_created'
                              RETURNING host_id`)
	if err != nil {
		return Hosts{}, err
	}

	hosts := Hosts{}
	for rows.Next() {
		var hostId int64
		if err := rows.Scan(&hostId); err != nil {
			return Hosts{}, err
		}
		host, err := GetHost(db, hostId)
		if err != nil {
			return Hosts{}, err
		}
		hosts = append(hosts, host)
	}

	if err = rows.Err(); err != nil {
		return Hosts{}, err
	}
	return hosts, nil
}

func CanHostCreateEvent(db *sql.DB, hostId int64) (bool, error) {
	rows, err := db.Query(
		`SELECT event_creation_invites.host_id,
                event_creation_invites.status
         FROM event_creation_invites
         WHERE event_creation_invites.status = 'pending'
         AND event_creation_invites.host_id = $1`, hostId)

	if err != nil {
		return false, err
	}

	for rows.Next() {
		return true, nil
	}

	return false, nil
}
