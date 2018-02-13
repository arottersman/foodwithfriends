package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"errors"
)

func idFromStr(idStr string) (int64, error) {
	if len(idStr) == 0 {
		return 0, errors.New("Empty string")
	}
	return strconv.ParseInt(idStr, 10, 64)
}

// TODO check that access token usr and usr match

// TODO
// func handleCreate(w http.ResponseWriter,
//                     r *http.Request,
//                     type,
//                     validator,
//                     createFunc) {
//
//                     }

func EventHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		if len(r.URL.Query().Get("eventId")) > 0 {
			HandleEventDetails(w, r)
		} else if len(r.URL.Query().Get("userId")) > 0 {
			HandleEventsForUser(w, r)
		} else if len(r.URL.Query()) == 0 {
			HandleCurrentEvents(w, r)
		} else {
			http.Error(w, "Not supported", 500)
		}
	} else if r.Method == "POST" {
		if strings.HasSuffix(r.URL.Path, "add-participant/") {
			HandleAddParticipantToEvent(w, r)
		} else if strings.HasSuffix(r.URL.Path, "cant-host/") {
			HandleCantHostEvent(w, r)
		} else {
			HandleEditEvent(w, r)
		}
	} else if r.Method == "PUT" {
		HandleCreateEvent(w, r)
	} else {
		http.Error(w, "Not supported", 500)
	}
}

func UserHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		if len(r.URL.Query().Get("auth0Id")) > 0 {
			HandleUserDetails(w, r.URL.Query().Get("auth0Id"))
		} else {
			http.Error(w, "Not supported", 500)
		}
	} else if r.Method == "POST" {
		HandleEditUser(w, r)
	} else if r.Method == "PUT" {
		HandleCreateUser(w, r)
	} else {
		http.Error(w, "Not supported", 500)
	}
}

func HostHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		if len(r.URL.Query().Get("hostId")) > 0 {
			HandleHostDetails(w, r)
		} else if len(r.URL.Query().Get("address")) > 0 {
			HandleSearchHostByAddress(w, r)
		} else {
			http.Error(w, "Not supported", 500)
		}
	} else if r.Method == "POST" {
		if strings.HasSuffix(r.URL.Path, "user/") &&
			len(r.URL.Query().Get("hostId")) > 0 {
			HandleAddUserToHost(w, r)
		} else {
			HandleEditHost(w, r)
		}
	} else if r.Method == "PUT" {
		HandleCreateHost(w, r)
	} else {
		http.Error(w, "Not supported", 500)
	}
}

func AdminHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method == "POST" {
		if strings.HasSuffix(r.URL.Path, "invites/") &&
			len(r.URL.Query().Get("numHosts")) > 0 {
			HandleSendItsYourTurnEmails(w, r)
		} else {
			fmt.Println("url path ", r.URL.Path)
			fmt.Println("query", r.URL.Query().Get("numHosts"))
			http.Error(w, "Not supported", 500)
		}
	} else {
		http.Error(w, "Not supported", 500)
	}
}

func HandleEventDetails(w http.ResponseWriter, r *http.Request) {
	eventId, err := idFromStr(r.URL.Query().Get("eventId"))
	if err != nil {
		http.Error(w, "Invalid eventId", 400)
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	event, err := GetEvent(db, eventId)
	db.Close()
	if err != nil {
		http.Error(w, "Couldn't get event", 400)
		return
	}

	json.NewEncoder(w).Encode(event)
}

func HandleCreateEvent(w http.ResponseWriter, r *http.Request) {
	var event Event

	if r.Body == nil {
		http.Error(w, "No request body", 400)
		return
	}

	err := json.NewDecoder(r.Body).Decode(&event)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	err = ValidateEvent(event)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}
	canCreate, err := CanHostCreateEvent(db, event.Host.HostId)
	if err != nil || !canCreate {
		http.Error(w, "Not user's turn to create event", 400)
		return
	}

	eventId, err := CreateEvent(db, event)
	if err != nil {
		http.Error(w, "Couldn't create event", 400)
		return
	}

	event.EventId = eventId
	json.NewEncoder(w).Encode(event)

	UpdateHostInvitation(db, event.Host.HostId, EVENT_CREATED)
	db.Close()
}

func HandleCantHostEvent(w http.ResponseWriter, r *http.Request) {
	hostId, err := idFromStr(r.URL.Query().Get("hostId"))
	if err != nil {
		http.Error(w, "Invalid hostId", 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	canCreate, err := CanHostCreateEvent(db, hostId)
	if err != nil || !canCreate {
		http.Error(w, "Not user's turn to create event", 400)
		return
	}

	err = UpdateHostInvitation(db, hostId, PASS)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	err = SendEmailsToLeastRecentHosts(db, 1)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	db.Close()
}

func HandleEditEvent(w http.ResponseWriter, r *http.Request) {
	var event Event

	if r.Body == nil {
		http.Error(w, "No request body", 400)
		return
	}

	err := json.NewDecoder(r.Body).Decode(&event)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	updatedEvent, err := UpdateEvent(db, event)
	db.Close()
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	json.NewEncoder(w).Encode(updatedEvent)
}

func HandleAddParticipantToEvent(w http.ResponseWriter, r *http.Request) {
	userId, err := idFromStr(r.URL.Query().Get("userId"))
	if err != nil {
		http.Error(w, "Invalid userId", 400)
	}

	eventId, err := idFromStr(r.URL.Query().Get("eventId"))
	if err != nil {
		http.Error(w, "Invalid eventId", 400)
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	updatedEvent, err := AddUserToEvent(db, eventId, userId)
	if err != nil {
		http.Error(w, "Couldn't add user to event", 400)
	}
	db.Close()

	json.NewEncoder(w).Encode(updatedEvent)
}

func HandleCurrentEvents(w http.ResponseWriter, r *http.Request) {
	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	events, err := GetCurrentEvents(db)
	db.Close()
	if err != nil {
		http.Error(w, err.Error(), 500)
		// http.Error(w, "Couldn't get current events", 500)
		return
	}

	json.NewEncoder(w).Encode(events)
}

func HandleEventsForUser(w http.ResponseWriter, r *http.Request) {
	userId, err := idFromStr(r.URL.Query().Get("userId"))
	if err != nil {
		http.Error(w, "Invalid userId", 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	events, err := GetPastEventsForUser(db, userId)
	db.Close()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	json.NewEncoder(w).Encode(events)
}

func HandleUserDetails(w http.ResponseWriter, auth0Id string) {
	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	user, err := GetUserByAuth0Id(db, auth0Id)
	db.Close()

	if err == sql.ErrNoRows {
		http.Error(w, "No account for this user id", 404)
		return
	}
	if err != nil {
		http.Error(w, "Couldn't get user", 400)
		fmt.Printf("%s\n", err.Error())
		return
	}

	json.NewEncoder(w).Encode(user)
}

func HandleCreateUser(w http.ResponseWriter, r *http.Request) {
	var user User

	if r.Body == nil {
		http.Error(w, "No request body", 400)
		return
	}

	err := json.NewDecoder(r.Body).Decode(&user)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	err = ValidateUser(user)
	if err != nil {
		http.Error(w, err.Error(), 400)
		fmt.Printf("%s", err.Error())
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, "Couldn't connect to DB", 500)
		fmt.Printf("%s", err.Error())
		return
	}

	userId, err := CreateUser(db, user)
	db.Close()
	if err != nil {
		http.Error(w, "Couldn't create user", 400)
		fmt.Printf("%s", err.Error())
		return
	}

	user.UserId = userId
	json.NewEncoder(w).Encode(user)
}

func HandleEditUser(w http.ResponseWriter, r *http.Request) {
	var user User

	if r.Body == nil {
		http.Error(w, "No request body", 400)
		return
	}

	err := json.NewDecoder(r.Body).Decode(&user)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	updatedUser, err := UpdateUser(db, user)
	db.Close()
	if err != nil {
		http.Error(w, "Couldn't update user", 400)
		fmt.Printf("%s", err.Error())
		return
	}

	json.NewEncoder(w).Encode(updatedUser)
}

func HandleCreateHost(w http.ResponseWriter, r *http.Request) {
	var host Host

	if r.Body == nil {
		http.Error(w, "No request body", 400)
		return
	}

	err := json.NewDecoder(r.Body).Decode(&host)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	err = ValidateHost(host)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	hostId, err := CreateHost(db, host)
	db.Close()
	if err != nil {
		http.Error(w, "Couldn't create host", 400)
		fmt.Printf("%s\n", err.Error())
		return
	}

	host.HostId = hostId
	json.NewEncoder(w).Encode(host)
}

func HandleEditHost(w http.ResponseWriter, r *http.Request) {
	var host Host

	if r.Body == nil {
		http.Error(w, "No request body", 400)
		return
	}

	err := json.NewDecoder(r.Body).Decode(&host)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	updatedHost, err := UpdateHost(db, host)
	db.Close()
	if err != nil {
		http.Error(w, "Couldn't update host", 400)
		return
	}

	json.NewEncoder(w).Encode(updatedHost)
}

func HandleHostDetails(w http.ResponseWriter, r *http.Request) {
	hostId, err := idFromStr(r.URL.Query().Get("hostId"))
	if err != nil {
		http.Error(w, "Invalid hostId", 400)
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	host, err := GetHost(db, hostId)
	db.Close()
	if err != nil {
		http.Error(w, "Couldn't get host", 400)
		return
	}

	json.NewEncoder(w).Encode(host)
}

func HandleSearchHostByAddress(w http.ResponseWriter, r *http.Request) {
	address := r.URL.Query().Get("address")

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	hosts, err := GetHostsByAddress(db, address)
	db.Close()
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	json.NewEncoder(w).Encode(hosts)
}

func HandleAddUserToHost(w http.ResponseWriter, r *http.Request) {
	hostId, err := idFromStr(r.URL.Query().Get("hostId"))
	if err != nil {
		http.Error(w, "Invalid hostId", 400)
		return
	}

	var user User

	if r.Body == nil {
		http.Error(w, "No request body", 400)
		return
	}

	err = json.NewDecoder(r.Body).Decode(&user)
	if err != nil {
		http.Error(w, err.Error(), 400)
		return
	}

	if user.UserId == 0 {
		http.Error(w, "Must provide a userId", 400)
		return
	}

	db, err := Connect()
	if err != nil {
		http.Error(w, err.Error(), 500)
		return
	}

	host, err := AddUserToHost(db, hostId, user.UserId)
	db.Close()
	if err != nil {
		http.Error(w, "Couldn't add user to host", 400)
		return
	}

	json.NewEncoder(w).Encode(host)
}

func HandleSendItsYourTurnEmails(w http.ResponseWriter, r *http.Request) {
	numHostsStr := r.URL.Query().Get("numHosts")
	numHosts, err := strconv.Atoi(numHostsStr)
	if err != nil {
		http.Error(w, "Invalid numHosts", 400)
		return
	}

	db, err := Connect()
	if err != nil {
		fmt.Println(err)
		http.Error(w, "Couldn't send emails", 500)
		return
	}

	err = SendEmailsToLeastRecentHosts(db, numHosts)
	if err != nil {
		fmt.Println(err)
		http.Error(w, "Couldn't send emails", 500)

		return
	}
}
