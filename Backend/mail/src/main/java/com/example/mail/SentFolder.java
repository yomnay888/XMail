package com.example.mail;

import java.util.ArrayList;

public class SentFolder implements MailFolders{
    private ArrayList<Mail> sentMails;

    @Override
    public void addMail(Mail mail) {

    }

    @Override
    public ArrayList<Mail> getMail() {
        return null;
    }

    @Override
    public ArrayList<Mail> deleteMail(Mail mail) {
        return null;
    }
}
