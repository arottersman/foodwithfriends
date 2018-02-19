package main

import (
	"database/sql"
	"math/rand"
	"reflect"
	"testing"
	"time"
)

func DeleteEverything(db *sql.DB) {
	db.Exec("DELETE FROM event_creation_invites")
	db.Exec("DELETE FROM event_users")
	db.Exec("DELETE FROM host_users")
	db.Exec("DELETE FROM events")
	db.Exec("DELETE FROM hosts")
	db.Exec("DELETE FROM users")
}

// User

var src = rand.NewSource(time.Now().UnixNano())

const letterBytes = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
const (
	letterIdxBits = 6                    // 6 bits to represent a letter index
	letterIdxMask = 1<<letterIdxBits - 1 // All 1-bits, as many as letterIdxBits
	letterIdxMax  = 63 / letterIdxBits   // # of letter indices fitting in 63 bits
)

func RandStringBytesMaskImprSrc(n int) string {
	b := make([]byte, n)
	// A src.Int63() generates 63 random bits, enough for letterIdxMax characters!
	for i, cache, remain := n-1, src.Int63(), letterIdxMax; i >= 0; {
		if remain == 0 {
			cache, remain = src.Int63(), letterIdxMax
		}
		if idx := int(cache & letterIdxMask); idx < len(letterBytes) {
			b[i] = letterBytes[idx]
			i--
		}
		cache >>= letterIdxBits
		remain--
	}

	return string(b)
}

func GetTestUser() User {
	return User{
		Name:                "Alice Rottersman",
		Email:               RandStringBytesMaskImprSrc(5) + "gmail.com",
		DietaryRestrictions: []string{"strawberries", "nuts"},
		Auth0Id:             RandStringBytesMaskImprSrc(5),
	}
}

func AreUsersEqual(user1 User, user2 User) bool {
	return user1.Name == user2.Name &&
		user1.Email == user2.Email &&
		reflect.DeepEqual(user1.DietaryRestrictions,
			user2.DietaryRestrictions)
}

func TestCreateReadUser(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeUser := GetTestUser()
	CreateUser(db, fakeUser)

	userFromDb, err := GetUserByAuth0Id(db, fakeUser.Auth0Id)
	if err != nil {
		t.Error(err)
	}

	if !AreUsersEqual(userFromDb, fakeUser) {
		t.Errorf("User in DB doesn't match created user: \n  %s \n %s \n",
			userFromDb,
			fakeUser)
	}

	DeleteEverything(db)
}

func TestEditUser(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}
	fakeUser := GetTestUser()
	_, err = CreateUser(db, fakeUser)

	if err != nil {
		t.Error(err)
	}

	fakeUser.Email = "AnotherEmail"
	fakeUser.DietaryRestrictions = []string{"blueberries", "nuts"}

	editedFakeDbUser, err := UpdateUser(db, fakeUser)

	if err != nil {
		t.Error(err)
	}

	if !AreUsersEqual(editedFakeDbUser, fakeUser) {
		t.Errorf("User in DB doesn't match created user: \n  %s \n %s \n",
			editedFakeDbUser,
			fakeUser)
	}

	DeleteEverything(db)
	db.Close()
}

// Host

func GetTestHost() Host {
	return Host{
		Address:      "123 Market St",
		City:         "Philadelphia",
		State:        "PA",
		MaxOccupancy: 7,
		Zipcode:      "19147",
	}
}

func UsersContainsId(users Users, userId int64) bool {
	for _, user := range users {
		if user.UserId == userId {
			return true
		}
	}
	return false
}

func AreUserIdsEqual(users1 Users, users2 Users) bool {
	if len(users1) != len(users2) {
		return false
	}
	for _, user := range users2 {
		if !UsersContainsId(users1, user.UserId) {
			return false
		}
	}
	return true
}

func AreHostsEqual(host1 Host, host2 Host) bool {
	return host1.Address == host2.Address &&
		host1.City == host2.City &&
		host1.State == host2.State &&
		host1.MaxOccupancy == host2.MaxOccupancy &&
		host1.Zipcode == host2.Zipcode &&
		AreUserIdsEqual(host1.Users,
			host2.Users)
}

func CreateFakeHost(db *sql.DB) (Host, error) {
	fakeUser := GetTestUser()
	userId, err := CreateUser(db, fakeUser)

	if err != nil {
		return Host{}, err
	}

	fakeHost := GetTestHost()
	fakeHost.Users = Users{User{UserId: userId}}
	hostId, err := CreateHost(db, fakeHost)
	fakeHost.HostId = hostId
	return fakeHost, err
}

func TestCreateReadHost(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeHost, err := CreateFakeHost(db)

	if err != nil {
		t.Error(err)
	}

	fakeDbHost, err := GetHost(db, fakeHost.HostId)

	if err != nil {
		t.Error(err)
	}

	if !AreHostsEqual(fakeDbHost, fakeHost) {
		t.Errorf("Host in DB doesn't match created host: \n  %s \n %s \n",
			fakeDbHost,
			fakeHost)
	}

	DeleteEverything(db)
	db.Close()
}

func TestGetLeastRecentHosts(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	event1, err := CreateFakeEvent(db, GetFakeEvent())
	if err != nil {
		t.Error(err)
	}

	host1 := event1.Host

	event2, err := CreateFakeEvent(db, GetFakeEvent())
	if err != nil {
		t.Error(err)
	}

	host2 := event2.Host

	// Create a third event that is the *most* recent
	_, err = CreateFakeEvent(db, GetFakeEvent())
	if err != nil {
		t.Error(err)
	}

	leastRecentHosts := Hosts{host1, host2}

	leastRecentDbHosts, err := GetLeastRecentHosts(db, 2)
	if err != nil {
		t.Error(err)
	}

	if len(leastRecentHosts) != len(leastRecentDbHosts) {
		t.Errorf(`Got wrong number of recent hosts
                          len %d:  %s

                          len %d:  %s`,
			len(leastRecentHosts), leastRecentHosts,
			len(leastRecentDbHosts), leastRecentDbHosts)
	}

	if !AreHostsEqual(leastRecentHosts[0], leastRecentDbHosts[0]) ||
		!AreHostsEqual(leastRecentHosts[1], leastRecentDbHosts[1]) {
		t.Errorf(`Got wrong most recent hosts

                         %s

                         %s`, leastRecentHosts, leastRecentDbHosts)
	}

	DeleteEverything(db)
	db.Close()
}

func TestAddUserToHost(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeHost, err := CreateFakeHost(db)

	if err != nil {
		t.Error(err)
	}

	anotherFakeUser := User{
		Name:    "Kevin Durant",
		Email:   "KD@goldenstate.com",
		Auth0Id: "19191",
	}

	userId, err := CreateUser(db, anotherFakeUser)
	if err != nil {
		t.Error(err)
	}

	anotherFakeUser.UserId = userId

	fakeDbHost, err := AddUserToHost(db, fakeHost.HostId, userId)

	if err != nil {
		t.Error(err)
	}

	fakeHost.Users = append(fakeHost.Users, anotherFakeUser)

	if !AreHostsEqual(fakeDbHost, fakeHost) {
		t.Errorf("Host in DB doesn't match created host: \n  %s \n %s \n",
			fakeDbHost,
			fakeHost)
	}

	DeleteEverything(db)
	db.Close()
}

func TestEditHost(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeHost, err := CreateFakeHost(db)

	if err != nil {
		t.Error(err)
	}

	fakeHost.Address = "Another Address"
	fakeHost.MaxOccupancy = 1241

	editedFakeDbHost, err := UpdateHost(db, fakeHost)

	if err != nil {
		t.Error(err)
	}

	if !AreHostsEqual(editedFakeDbHost, fakeHost) {
		t.Errorf(`Host in DB doesn't match edited host

                        %s

                        %s`,
			editedFakeDbHost,
			fakeHost)
	}

	DeleteEverything(db)
	db.Close()
}

// Events

func AreEventsEqual(event1 Event, event2 Event) bool {
	return event1.Title == event2.Title &&
		event1.Description == event2.Description &&
		event1.HappeningAt.Round(time.Millisecond).Equal(
			event2.HappeningAt.Round(time.Millisecond)) &&
		AreHostsEqual(event1.Host, event2.Host) &&
		AreUserIdsEqual(event1.Participants, event2.Participants)
}

func GetFakeEvent() Event {
	return Event{
		Title:       "Amazing Event",
		Description: "The best event ever",
		HappeningAt: time.Now().AddDate(0, 1, 0),
	}
}

func CreateFakeEvent(db *sql.DB, event Event) (Event, error) {
	fakeHost, err := CreateFakeHost(db)

	if err != nil {
		return Event{}, err
	}

	err = AddHostInvitations(db, Hosts{fakeHost})
	if err != nil {
		return Event{}, err
	}

	event.Host = fakeHost
	eventId, err := CreateEvent(db, event)
	if err != nil {
		return Event{}, err
	}
	event.EventId = eventId
	err = UpdateHostInvitation(db, fakeHost.HostId, EVENT_CREATED)
	if err != nil {
		return Event{}, err
	}

	return event, nil
}

func TestCreateReadEvent(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeEvent, err := CreateFakeEvent(db, GetFakeEvent())

	if err != nil {
		t.Error(err)
	}

	fakeDbEvent, err := GetEvent(db, fakeEvent.EventId)

	if err != nil {
		t.Error(err)
	}

	if !AreEventsEqual(fakeEvent, fakeDbEvent) {
		t.Errorf(`Event in DB doesn't match created event

                        %s

                        %s`,
			fakeDbEvent,
			fakeEvent)
	}

	DeleteEverything(db)
	db.Close()
}

func TestAddParticipantToEvent(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeEvent, err := CreateFakeEvent(db, GetFakeEvent())

	if err != nil {
		t.Error(err)
	}

	anotherFakeUser := User{
		Name:    "A User By Any Other Name",
		Email:   "email@email.org",
		Auth0Id: "191911919",
	}

	userId, err := CreateUser(db, anotherFakeUser)
	anotherFakeUser.UserId = userId

	if err != nil {
		t.Error(err)
	}

	fakeDbEvent, err := AddUserToEvent(db, fakeEvent.EventId, userId)

	if err != nil {
		t.Error(err)
	}

	fakeEvent.Participants = append(fakeEvent.Participants, anotherFakeUser)

	if !AreEventsEqual(fakeEvent, fakeDbEvent) {
		t.Errorf(`Event in DB doesn't match updated user event

                           %s

                           %s`,
			fakeDbEvent,
			fakeEvent)
	}

	if fakeDbEvent.Participants[0].AssignedDish == "" {
		t.Errorf("Didn't assign side dish to user!")
	}

	DeleteEverything(db)
	db.Close()
}

func TestReadCurrentEvents(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakePastEventPartial := GetFakeEvent()
	fakePastEventPartial.HappeningAt = time.Now().AddDate(-1, 0, 0)
	_, err = CreateFakeEvent(db, fakePastEventPartial)

	if err != nil {
		t.Error(err)
	}

	futureFakeEvent, err := CreateFakeEvent(db, GetFakeEvent())

	if err != nil {
		t.Error(err)
	}

	futureEvents := Events{futureFakeEvent}

	futureDbEvents, err := GetCurrentEvents(db)

	if err != nil {
		t.Error(err)
	}

	if len(futureEvents) != len(futureDbEvents) {
		t.Errorf(`Current events got the wrong number of events

                          len %d:  %s

                          len %d:  %s`,
			len(futureEvents), futureEvents,
			len(futureDbEvents), futureDbEvents)
	}

	if !AreEventsEqual(futureEvents[0], futureDbEvents[0]) {
		t.Errorf(`Current events got the wrong events

                          %s

                          %s`, futureEvents, futureDbEvents)
	}

	DeleteEverything(db)
	db.Close()
}

func TestReadEventsForUser(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeEvent, err := CreateFakeEvent(db, GetFakeEvent())
	if err != nil {
		t.Error(err)
	}

	fakeEventWithParticipant, err := CreateFakeEvent(db, GetFakeEvent())
	if err != nil {
		t.Error(err)
	}

	anotherFakeUser := User{
		Name:    "A User By Any Other Name",
		Email:   "email@email.org",
		Auth0Id: "191911919",
	}

	userId, createUserErr := CreateUser(db, anotherFakeUser)
	anotherFakeUser.UserId = userId

	if createUserErr != nil {
		t.Error(createUserErr)
	}

	_, err = AddUserToEvent(db, fakeEventWithParticipant.EventId, userId)
	if err != nil {
		t.Error(err)
	}

	fakeEventWithParticipant.Participants = append(
		fakeEventWithParticipant.Participants,
		anotherFakeUser,
	)

	// Test that we only get the events for the participant
	eventsWithAnotherUser := Events{fakeEventWithParticipant}

	eventsDbWithAnotherUser, err := GetPastEventsForUser(db, userId)
	if err != nil {
		t.Error(err)
	}

	if len(eventsWithAnotherUser) != len(eventsDbWithAnotherUser) {
		t.Errorf(`Events for user got the wrong number of events
                          user_id: %d

                          len %d:  %s

                          len %d:  %s`,
			userId,
			len(eventsWithAnotherUser), eventsWithAnotherUser,
			len(eventsDbWithAnotherUser), eventsDbWithAnotherUser)
	}

	if !AreEventsEqual(eventsWithAnotherUser[0], eventsDbWithAnotherUser[0]) {
		t.Errorf(`Events for user got the wrong events

                          %s

                          %s`, eventsWithAnotherUser, eventsDbWithAnotherUser[0])
	}

	// Test that we only get the events for a host user
	hostUserEvents := Events{fakeEvent}

	hostUserId := fakeEvent.Host.Users[0].UserId
	hostUserDbEvents, err := GetPastEventsForUser(db, hostUserId)
	if err != nil {
		t.Error(err)
	}

	if len(hostUserEvents) != len(hostUserDbEvents) {
		t.Errorf(`Events for user got the wrong number of events
                          user_id: %d

                          len %d:  %s

                          len %d:  %s`,
			hostUserId,
			len(hostUserEvents), hostUserEvents,
			len(hostUserDbEvents), hostUserDbEvents)
	}

	if !AreEventsEqual(hostUserEvents[0], hostUserDbEvents[0]) {
		t.Errorf(`Events for user got the wrong events

                          %s

                          %s`, hostUserEvents, hostUserDbEvents)
	}

	DeleteEverything(db)
	db.Close()
}

func TestEditEvent(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	fakeEvent, err := CreateFakeEvent(db, GetFakeEvent())

	if err != nil {
		t.Error(err)
	}

	fakeEvent.Title = "Another Title"
	fakeEvent.Description = "gunna be lit"

	editedFakeDbEvent, err := UpdateEvent(db, fakeEvent)

	if err != nil {
		t.Error(err)
	}

	if !AreEventsEqual(editedFakeDbEvent, fakeEvent) {
		t.Errorf("Event in DB doesn't match edited event \n  %s \n %s \n",
			editedFakeDbEvent,
			fakeEvent)
	}

	DeleteEverything(db)
	db.Close()
}

func TestSendEmail(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	aliceUser := User{
		Name:    "Alice Rottersman",
		Email:   "foodwithphriends@mailinator.com",
		Auth0Id: "1919191",
	}

	userId, err := CreateUser(db, aliceUser)

	aliceUser.UserId = userId

	if err != nil {
		t.Error(err)
	}

	_, err = CreateHost(db, Host{
		Address: "743 South Darien Str",
		Users:   Users{aliceUser},
	})

	if err != nil {
		t.Error(err)
	}

	leastRecentHosts, _ := GetLeastRecentHosts(db, 1)

	err = SendEmailsToLeastRecentHosts(db, 1)
	if err != nil {
		t.Error(err)
	}

	pendingHosts, err := GetPendingHosts(db)

	if len(leastRecentHosts) != len(pendingHosts) ||
		!AreHostsEqual(leastRecentHosts[0], pendingHosts[0]) {
		t.Errorf(`least recent hosts should be the same as pending hosts:
                          %s

                          %s`, leastRecentHosts, pendingHosts)
	}

	DeleteEverything(db)
	db.Close()
}

func TestExpireEventInvitations(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	pendingHost, err := CreateFakeHost(db)
	if err != nil {
		t.Error(err)
	}

	eventCreatedHost, err := CreateFakeHost(db)
	if err != nil {
		t.Error(err)
	}

	err = AddHostInvitations(db, Hosts{pendingHost,
		eventCreatedHost})
	if err != nil {
		t.Error(err)
	}

	err = UpdateHostInvitation(db, eventCreatedHost.HostId, EVENT_CREATED)
	if err != nil {
		t.Error(err)
	}

	expiredHosts, err := ExpireEventInvitations(db)
	if err != nil {
		t.Error(err)
	}

	if len(expiredHosts) != 1 ||
		!AreHostsEqual(expiredHosts[0], eventCreatedHost) {
		t.Errorf(`Expected expired hosts to match event created host
                         %s
                         %s`, expiredHosts, eventCreatedHost)
	}

	DeleteEverything(db)
	db.Close()
}

func TestCheckUserCanCreateEvent(t *testing.T) {
	db, err := Connect()
	if err != nil {
		t.Error(err)
	}

	pendingHost, err := CreateFakeHost(db)
	if err != nil {
		t.Error(err)
	}

	uninvitedHost, err := CreateFakeHost(db)

	err = AddHostInvitations(db, Hosts{pendingHost})
	if err != nil {
		t.Error(err)
	}

	canPendingHostCreateEvent, err := CanHostCreateEvent(db, pendingHost.HostId)
	canUninvitedHostCreateEvent, err := CanHostCreateEvent(db, uninvitedHost.HostId)

	if canPendingHostCreateEvent == false {
		t.Errorf("Pending host should be able to create event")
	}

	if canUninvitedHostCreateEvent == true {
		t.Errorf("Uninvited host should *not* be able to create event")
	}

	DeleteEverything(db)
	db.Close()
}

func TestCreateEventCreationLink(t *testing.T) {
	// CREATE EMAILS_SENT table (host_id, email_sent_at, status (
	//  PENDING, EVENT_CREATED, PASS, COMPLETE)
	// next email round, all EVENT_CREATED get set to COMPLETE
	// pass (host_id) WHERE PENDING
	//  send emails, ( create w host_ids + pending)
	// 1. add to "sent emails table", invalidate all others send emails
	// 2. email contains link (fwf.com/create-event
	//a) create an event (check if host id is in current email list)
	//b) can't host this time (remove from list, find next least recent, send email)

	/// create host -- lookup auth id, look up host via auth id`

	//

	/*
		1. create table
		2. db func to create rows in table
		3. db func to invalidate all current event_created --> complete
		4a. create enum for email status, and struct for emailsentrecord
		4b. db func to update single row event by host_id and status (emailsentrecord)
		5. cant host
		    - use 4b to set status to reject, send new email
	*/
}
