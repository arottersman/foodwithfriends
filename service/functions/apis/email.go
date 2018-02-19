package main

import (
	"database/sql"
	"errors"
	"fmt"
	"net/smtp"
	"os"
	"strings"
)

func SendEmailsToLeastRecentHosts(db *sql.DB, numHosts int) error {
	leastRecentHosts, err := GetLeastRecentHosts(db, numHosts)
	if err != nil {
		return err
	}
	if len(leastRecentHosts) <= 0 && numHosts > 0 {
		return errors.New("Didn't find any hosts that haven't received emails yet.")
	}

	fwfEmail := os.Getenv("FWF_EMAIL")
	fwfEmailPassword := os.Getenv("FWF_EMAIL_PASSWORD")

	auth := smtp.PlainAuth("", fwfEmail, fwfEmailPassword, "smtp.gmail.com")

	var recipients []string
	for _, host := range leastRecentHosts {
		for _, user := range host.Users {
			recipients = append(recipients, user.Email)
		}
	}

	if len(recipients) <= 0 {
		return errors.New(fmt.Sprintf("Failed to find any recipients emails for least recent hosts, host.Users is %s", leastRecentHosts))
	}

	recipientsString := strings.Join(recipients, ",")

	msg := []byte("From: " + fwfEmail + "\r\n" +
		"To: " + recipientsString + "\r\n" +
		"Subject: Your turn to host! \r\n" +
		"\r\n" +
		"Please visit https://d6ye2sqzk9ylp.cloudfront.net/#create-event " +
		"to create your event. Only one of your house need create one. " +
		"Make sure to rsvp to the event yourself when you're done. " +
		"Thanks <3 \r\n")
	err = smtp.SendMail("smtp.gmail.com:587", auth, fwfEmail, recipients, msg)

	if err != nil {
		return err
	}

	err = AddHostInvitations(db, leastRecentHosts)
	if err != nil {
		return err
	}

	return nil
}

func EmailEventUpdates(updatedEvent Event) error {
	fwfEmail := os.Getenv("FWF_EMAIL")
	fwfEmailPassword := os.Getenv("FWF_EMAIL_PASSWORD")

	auth := smtp.PlainAuth("", fwfEmail, fwfEmailPassword, "smtp.gmail.com")

	var recipients []string
	for _, user := range updatedEvent.Participants {
		recipients = append(recipients, user.Email)
	}

	// Don't do anything if there are no
	// participants to email
	if len(recipients) <= 0 {
		return nil
	}

	recipientsString := strings.Join(recipients, ",")

	msg := []byte("From: " + fwfEmail + "\r\n" +
		"To: " + recipientsString + "\r\n" +
		"Subject: Your Potluck's Got An Update \r\n" +
		"\r\n" +
		"Hello. \n You're receiving this email because " +
		"you RSVPed to a potluck with the VFA potluck app." +
		"The hosts have updated the event. The info is now: \n" +
		"Time: " + updatedEvent.HappeningAt.Format("Mon January 2, 15:04") + "\n" +
		"Description: " + updatedEvent.Title + "\n" +
		"You can log onto the app for more info. \n Bye. \n\n" +
		"https://d6ye2sqzk9ylp.cloudfront.net/ \r\n")
	err := smtp.SendMail("smtp.gmail.com:587", auth, fwfEmail, recipients, msg)

	if err != nil {
		return err
	}

	return nil
}
