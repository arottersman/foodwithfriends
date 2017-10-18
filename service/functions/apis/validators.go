package main

import (
    "fmt"
    "errors"
)

func ValidateUser(user User) error {
    var missingFields []string
    if len(user.Name) == 0 {
        missingFields = append(missingFields, "name")
    }
    if len(user.Auth0Id) == 0 {
        missingFields = append(missingFields, "auth0Id")
    }
    if len(user.Email) == 0 {
        missingFields = append(missingFields, "email")
    }

    if len(missingFields) > 0 {
        err := fmt.Sprintf("Missing required fields: %s", missingFields)
        return errors.New(err)
    }
    return nil
}

func ValidateEvent(event Event) error {
    var missingFields []string
    if len(event.Title) == 0 {
        missingFields = append(missingFields, "title")
    }
    if event.Host.HostId == 0 {
        missingFields = append(missingFields, "host")
    }

    if len(missingFields) > 0 {
        err := fmt.Sprintf("Missing required fields: %s", missingFields)
        return errors.New(err)
    }
    return nil
}

func ValidateHost(host Host) error {
    var missingFields []string
    if len(host.Address) == 0 {
        missingFields = append(missingFields, "address")
    }
    if len(host.City) == 0 {
        missingFields = append(missingFields, "city")
    }
    if len(host.State) == 0 {
        missingFields = append(missingFields, "state")
    }
    if len(host.Zipcode) == 0 {
        missingFields = append(missingFields, "zipcode")
    }
    if host.MaxOccupancy == 0 {
        missingFields = append(missingFields, "maxOccupancy")
    }

    if len(host.Users) < 1 {
	    missingFields = append(missingFields, "users")
    } else {
	    for _, user := range host.Users {
		if user.UserId == 0 {
	            missingFields = append(missingFields,
			                   "users")
		}
	    }
    }

    if len(missingFields) > 0 {
        err := fmt.Sprintf("Missing required fields: %s", missingFields)
        return errors.New(err)
    }
    return nil
}
