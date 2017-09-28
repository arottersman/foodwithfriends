package main

import (
	"os"
	"fmt"
	"errors"
	"strings"
	"net/smtp"
	"database/sql"
)


func SendEmailsToLeastRecentHosts(db *sql.DB, numHosts int) (error) {
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
		"test 123 \r\n")
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
