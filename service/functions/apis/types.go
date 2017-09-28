package main

import (
    "time"
)

type Event struct {
    EventId        int64        `json:"eventId"`
    Title          string       `json:"title"`
    Description    string       `json:"description"`
    HappeningAt    time.Time    `json:"happeningAt"` // expects RFC3339
    Host           Host         `json:"host"`
    Participants   Users        `json:"participants"`
}

type Events []Event

type Host struct {
    HostId         int64      `json:"hostId"`
    Address        string     `json:"address"`
    City           string     `json:"city"`
    State          string     `json:"state"`
    Zipcode        string     `json:"zipcode"`
    MaxOccupancy   int64      `json:"maxOccupancy"`
    Users          Users      `json:"users"`
}

type Hosts []Host

type User struct {
    UserId              int64     `json:"userId,omitempty"`
    Name                string    `json:"name,omitempty"`
    Email               string    `json:"email,omitempty"`
    AssignedDish        string    `json:"assignedDish,omitempty"`
    Bringing            string    `json:"bringing,omitempty"`
    DietaryRestrictions []string  `json:"dietaryRestrictions,omitempty"`
    Auth0Id             string    `json:"auth0Id,omitempty"`
    HostId              int64     `json:"hostId,omitempty"`
}

type Users []User

// AWS Lambda / apex

type (
    LambdaInput struct {
        Body     string             `json:"body"`
        Headers  map[string]string  `json:"headers"`
        Method   string             `json:"httpMethod"`
        Path     string             `json:"path"`
        Params   map[string]string  `json:"queryStringParameters"`
    }

    LambdaOutput struct {
        StatusCode  int                `json:"statusCode"`
        Headers     map[string]string  `json:"headers"`
        Body        string             `json:"body"`
    }
)
