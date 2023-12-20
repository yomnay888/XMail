package com.example.mail;

import java.util.ArrayList;

public class InboxFolder implements MailFolders{
    private ArrayList<Mail> inboxMails;
    @Override
    public void addMail(Mail mail) {
        if(this.inboxMails == null)this.inboxMails = new ArrayList<Mail>();
        this.inboxMails.add(mail);
    }

    @Override
    public ArrayList<Mail> getMail() {
        return this.inboxMails;
    }

    public void setInboxMails(ArrayList<Mail> inboxMails) {
        this.inboxMails = inboxMails;
    }
    @Override
    public ArrayList<Mail> deleteMail(Mail mail) {
        return this.inboxMails;
    }
}
