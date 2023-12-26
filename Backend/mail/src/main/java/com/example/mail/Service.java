package com.example.mail;

import com.example.mail.filter.AndCriteria;
import com.example.mail.filter.ContactCriteria;
import com.example.mail.filter.SearchAllCriteria;
import com.example.mail.proxy.Xmail;
import com.example.mail.proxy.proxyXmail;
import com.example.mail.sortStrategy.SortStrategy;
import com.example.mail.sortStrategy.SortStrategyFactory;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

@org.springframework.stereotype.Service
public class Service {
    public User currentUser;
    private final RegisteredUsers registeredUsers = new RegisteredUsers();
    private final FileService file = new FileService();

    private Xmail xmail = new proxyXmail();

    public boolean signUp(UserDto user) throws NoSuchAlgorithmException {
        user.setPassword(Hashing.hashingPassword(user.getPassword()));
        User newUser = registeredUsers.addUser(user);
        if(newUser != null){
            file.generateJsonFile(newUser);
            this.currentUser = newUser;
            this.xmail.signIn(user.getEmail());
        }
        return newUser!=null;
    }
    public boolean signIn(UserDto user) throws NoSuchAlgorithmException {
        int id = registeredUsers.getUserId(user.getEmail());
        if (id!=0) {
            User newUser = file.getJsonData(id);
            if(Hashing.hashingPassword(user.getPassword()).equals(newUser.getPassword())) {
                this.xmail.signIn(newUser.getEmail());
                this.currentUser = newUser;
                return true;
            }
        }
        return false;
    }
    public void signOut(String email){
        this.xmail.signedOut(email);
        this.currentUser = null;
    }
    public SystemDto addMail(Mail mail){
        System.out.println("YES YES NEW MAIIIIIIIIIIL !!!!!!!!!!");
        if(mail.getMailType().equals("draft")){
            this.sendDraft(mail);
            mail.setMailType("sent");
        }
        MailBuilder mailBuilder = new MailBuilder();
        mailBuilder.setTo(mail.getTo()).setMailID(this.currentUser.getGlobalMailNumber()).setMailType(mail.getMailType()).setLocalDate().setLocalTime();
        mailBuilder.setPriority(mail.getPriority()).setPriority(mail.getPriority()).setFrom(mail.getFrom()).setSubject(mail.getSubject());
        if(mail.getAttachments()!=null)
            mailBuilder.setAttachments(mail.getAttachments());
        if(mail.getContent() != null)
            mailBuilder.setContent(mail.getContent());
        Mail newMail = mailBuilder.build();
        this.currentUser.getSentFolder();
        this.currentUser.addSent(newMail);
        System.out.println(this.currentUser.getSentFolder().getMail());
        file.generateJsonFile(currentUser);
//        mail.setFrom(this.currentUser.getEmail());
        mail.setMailType("inbox");
        setInbox(mail.getTo(), mail);
        //Coming soon...
//        setInbox(mail.getCc(),mail);
//        setInbox(mail.getBcc(),mail);
        SystemDto systemDto = new SystemDto();
        systemDto.setSourceMails(this.currentUser.getSentFolder().getMail());
        systemDto.setDestinationMails(this.currentUser.getDraftFolder().getMail());
        return systemDto;
    }
    public DraftFolder draftMail(Mail mail){
        System.out.println("Drafted hehe");
        System.out.println(mail.getMailID());
        if(mail.getMailID() != -1){
            //                    int index = this.currentUser.getDraftFolder().getMail().indexOf(draft);
            this.currentUser.getDraftFolder().getMail().removeIf(draft -> draft.getMailID() == mail.getMailID());
        }
        MailBuilder mailBuilder = new MailBuilder();
        mailBuilder.setTo(mail.getTo()).setMailID(this.currentUser.getGlobalMailNumber()).setMailType(mail.getMailType()).setLocalDate().setLocalTime();
        mailBuilder.setPriority(mail.getPriority()).setPriority(mail.getPriority()).setFrom(mail.getFrom()).setSubject(mail.getSubject());
        if(mail.getAttachments()!=null)
            mailBuilder.setAttachments(mail.getAttachments());
        if(mail.getContent() != null)
            mailBuilder.setContent(mail.getContent());
        Mail newMail = mailBuilder.build();
        mail.setMailType("draft");
        this.currentUser.getDraftFolder();
        this.currentUser.addDraft(newMail);
        file.generateJsonFile(currentUser);
//        mail.setFrom(this.currentUser.getEmail());
        return this.currentUser.getDraftFolder();
    }
    public SystemDto trashMail(ArrayList<Mail> mails, String source){
        SystemDto systemDto = new SystemDto();
        for (Mail mail : mails){
            System.out.println("TRASH WORKED LESGOOOO");
            MailBuilder mailBuilder = new MailBuilder();
            mailBuilder.setTo(mail.getTo()).setMailID(mail.getMailID()).setMailType(mail.getMailType());
            mailBuilder.setPriority(mail.getPriority()).setPriority(mail.getPriority()).setFrom(mail.getFrom()).setSubject(mail.getSubject());
            mailBuilder.setOldLocalDate(mail.getLocalDate()).setOldLocalTime(mail.getLocalTime());
            if(mail.getAttachments()!=null)
                mailBuilder.setAttachments(mail.getAttachments());
            if(mail.getContent() != null)
                mailBuilder.setContent(mail.getContent());
            Mail newMail = mailBuilder.build();
            this.currentUser.setTrashFolder(this.currentUser.getTrashFolder());
            this.currentUser.addTrash(newMail);
            System.out.println(mail.getMailType());
            if(mail.getMailType().equals("inbox")){
                this.currentUser.getInboxFolder().getMail().removeIf(mailToFind -> mailToFind.getMailID() == newMail.getMailID());
            } else if (mail.getMailType().equals("sent")){
                this.currentUser.getSentFolder().getMail().removeIf(mailToFind -> mailToFind.getMailID() == newMail.getMailID());
            } else if (mail.getMailType().equals("draft")) {
                this.currentUser.getDraftFolder().getMail().removeIf(mailToFind -> mailToFind.getMailID() == newMail.getMailID());
            }else{
                for(CustomFolder folder:this.currentUser.getCustomFolders()){
                    if(folder.getFolderName().equals(mail.getMailType())){
                        folder.getMail().removeIf(mailToFind -> mailToFind.getMailID() == mail.getMailID());
                        break;
                    }
                }
            }
        }
        if (source.equals("inbox"))
            systemDto.setSourceMails(this.currentUser.getInboxFolder().getMail());
        else if (source.equals("sent"))
            systemDto.setSourceMails(this.currentUser.getSentFolder().getMail());
        else if (source.equals("draft")) {
            systemDto.setSourceMails(this.currentUser.getDraftFolder().getMail());
        }
        else{
            for(CustomFolder folder:this.currentUser.getCustomFolders()){
                if(folder.getFolderName().equals(source)){
                    systemDto.setSourceMails(folder.getMail());
                    break;
                }
            }
        }
        System.out.println(this.currentUser.getInboxFolder().getMail());
        System.out.println(systemDto.getSourceMails());
        systemDto.setDestinationMails(this.currentUser.getTrashFolder().getMail());
        file.generateJsonFile(currentUser);
        return systemDto;
    }
    public User restoreFromTrash(ArrayList<Mail> mails){
        for (Mail mail : mails){
            MailBuilder mailBuilder = new MailBuilder();
            mailBuilder.setTo(mail.getTo()).setMailID(mail.getMailID()).setMailType(mail.getMailType());
            mailBuilder.setPriority(mail.getPriority()).setPriority(mail.getPriority()).setFrom(mail.getFrom()).setSubject(mail.getSubject());
            mailBuilder.setOldLocalDate(mail.getLocalDate()).setOldLocalTime(mail.getLocalTime());
            if(mail.getAttachments()!=null)
                mailBuilder.setAttachments(mail.getAttachments());
            if(mail.getContent() != null)
                mailBuilder.setContent(mail.getContent());
            Mail newMail = mailBuilder.build();
            if (newMail.getMailType().equals("inbox")){
                this.currentUser.addInbox(newMail);
            }else if (newMail.getMailType().equals("sent")){
                this.currentUser.addSent(newMail);
            }else if (newMail.getMailType().equals("draft")){
                this.currentUser.addDraft(newMail);
            }
            else{
                for(CustomFolder folder:this.currentUser.getCustomFolders()){
                    if(folder.getFolderName().equals(newMail.getMailType())){
                        folder.getMail().add(newMail);
                        break;
                    }
                }
            }
            this.currentUser.getTrashFolder().getMail().removeIf(trashMail -> trashMail.getMailID() == newMail.getMailID());
        }
        file.generateJsonFile(this.currentUser);
        return this.currentUser;
    }
    public SystemDto deleteFromTrash(ArrayList<Mail> mails){
        for (Mail mail : mails){
            MailBuilder mailBuilder = new MailBuilder();
            mailBuilder.setTo(mail.getTo()).setMailID(mail.getMailID()).setMailType(mail.getMailType());
            mailBuilder.setPriority(mail.getPriority()).setPriority(mail.getPriority()).setFrom(mail.getFrom()).setSubject(mail.getSubject());
            mailBuilder.setOldLocalDate(mail.getLocalDate()).setOldLocalTime(mail.getLocalTime());
            if(mail.getAttachments()!=null)
                mailBuilder.setAttachments(mail.getAttachments());
            if(mail.getContent() != null)
                mailBuilder.setContent(mail.getContent());
            Mail newMail = mailBuilder.build();
            this.currentUser.getTrashFolder().getMail().removeIf(trashMail -> trashMail.getMailID() == newMail.getMailID());
        }
        SystemDto systemDto = new SystemDto();
        systemDto.setDestinationMails(this.currentUser.getTrashFolder().getMail());
        systemDto.setSourceMails(this.currentUser.getTrashFolder().getMail());
        System.out.println(this.currentUser.getTrashFolder().getMail());
//        return this.currentUser.getTrashFolder();
        System.out.println("Delete From Trash");
        file.generateJsonFile(this.currentUser);
        return systemDto;
    }
    public User getUser(String email){
        int id = registeredUsers.getUserId(email);
        if (id!=0) {
            return file.getJsonData(id);
        }
        return null;
    }
    public void setInbox(ArrayList<String> emails, Mail mail){
        for(String email:emails) {
            User user = getUser(email);
            if (user != null) {
                user.getInboxFolder();
                mail.setMailID(user.getGlobalMailNumber());
                user.addInbox(mail);
                file.generateJsonFile(user);
            }
        }
    }

    public boolean setCurrentUser(User currentUser) {
        System.out.println(currentUser.getEmail());
        this.currentUser = this.xmail.checkeUser(currentUser.getEmail());
        System.out.println(this.currentUser);
        return this.currentUser != null;
    }
    public void sendDraft(Mail mail){
        //                int index = this.currentUser.getDraftFolder().getMail().indexOf(draft);
        this.currentUser.getDraftFolder().getMail().removeIf(draft -> draft.getMailID() == mail.getMailID());
//        mail.setMailType("sent");
//        this.addMail(mail);
//        return this.currentUser.getDraftFolder();
    }
    public ArrayList<Mail> search(HashMap<String,String> hashMap, ArrayList<Mail> mails){
        SearchAllCriteria searchAllCriteria = new SearchAllCriteria();
        return searchAllCriteria.matchesCriteria(mails, hashMap);
    }
    public ArrayList<Mail> filter(HashMap<String,String> hashMap, ArrayList<Mail> mails){
        AndCriteria andCriteria = new AndCriteria();
        return andCriteria.meetCriteria(mails, hashMap);
    }
    public ArrayList<CustomFolder> createCustomFolder(String folderName){
        this.currentUser.getCustomFolders();
        this.currentUser.createCustomFolder(folderName);
        this.file.generateJsonFile(this.currentUser);
        return this.currentUser.getCustomFolders();
    }
    public void bulkMove(Mail mail){
        if(mail.getMailType().equals("inbox")){
            this.currentUser.getInboxFolder().getMail().removeIf(mailToFind -> mailToFind.getMailID() == mail.getMailID());
        } else if (mail.getMailType().equals("sent")){
            this.currentUser.getSentFolder().getMail().removeIf(mailToFind -> mailToFind.getMailID() == mail.getMailID());
        } else if (mail.getMailType().equals("draft")) {
            this.currentUser.getDraftFolder().getMail().removeIf(mailToFind -> mailToFind.getMailID() == mail.getMailID());
        }
        else{
            for(CustomFolder folder:this.currentUser.getCustomFolders()){
                if(folder.getFolderName().equals(mail.getMailType())){
                    folder.getMail().removeIf(mailToFind -> mailToFind.getMailID() == mail.getMailID());
                    break;
                }
            }
        }
    }
    public ArrayList<CustomFolder> addToCustom(String folderName, ArrayList<Mail> mails){
//        ArrayList<CustomFolder> customFolders = this.currentUser.getCustomFolders();
        for(CustomFolder folder:this.currentUser.getCustomFolders()){
            if(folder.getFolderName().equals(folderName)){
                for(Mail mail:mails){
                    this.bulkMove(mail);
                    mail.setMailType(folderName);
                    folder.addMail(mail);
                }
                break;
            }
        }
        this.currentUser.setCustomFolders(this.currentUser.getCustomFolders());
        this.file.generateJsonFile(this.currentUser);
        return this.currentUser.getCustomFolders();
    }
    public ArrayList<CustomFolder> removeFolder(String folderName){
        this.currentUser.getCustomFolders().removeIf(folder -> folder.getFolderName().equals(folderName));
        this.file.generateJsonFile(this.currentUser);
        return this.currentUser.getCustomFolders();
    }
    public ArrayList<CustomFolder> renameFolder(String oldName, String newName){
        for(CustomFolder folder:this.currentUser.getCustomFolders()){
            if(folder.getFolderName().equals(oldName)){
                folder.setFolderName(newName);
                break;
            }
        }
        this.file.generateJsonFile(this.currentUser);
        return this.currentUser.getCustomFolders();
    }
    public ArrayList<Contact> addContact(Contact contact){
        //3rfa enha doesn't make sense bas 3mlaha bec of serialization
        Contact newContact = new Contact();
        newContact.setName(contact.getName());
        newContact.setEmails(contact.getEmails());
        this.currentUser.addContact(newContact);
        file.generateJsonFile(currentUser);
        return this.currentUser.getContacts();
    }
    public ArrayList<Contact> editContact(SystemDto systemDto){
        //if didn't work do as serialization
        Contact newContact = new Contact();
        newContact.setName(systemDto.getContact().getName());
        newContact.setEmails(systemDto.getContact().getEmails());
        for (Contact contactToFind : this.currentUser.getContacts()){
            if (contactToFind.getName().equals(systemDto.getSource())){
                contactToFind.setEmails(newContact.getEmails());
                contactToFind.setName(systemDto.getDestination());
                System.out.println("hii");
                break;
            }
        }
        file.generateJsonFile(currentUser);
        return this.currentUser.getContacts();
    }
    public ArrayList<Contact> deleteContact(SystemDto systemDto){
        this.currentUser.getContacts().removeIf(contact -> contact.getName().equals(systemDto.getSource()));
        file.generateJsonFile(currentUser);
        return this.currentUser.getContacts();
    }
    public ArrayList<Contact> searchContacts(SystemDto systemDto){
        ContactCriteria contactCriteria = new ContactCriteria();
        return contactCriteria.meetCriteria(this.currentUser.getContacts(),systemDto.getSource());
    }
    public ArrayList<Contact> sortContacts(){
        ArrayList<Contact> sortedContacts = new ArrayList<>(this.currentUser.getContacts());
        Collections.sort(sortedContacts, Comparator.comparing(Contact::getName));
        return sortedContacts;
    }
    public ArrayList<Mail>defaultOrPriority(SystemDto systemDto){
        SortStrategyFactory sortStrategyFactory = new SortStrategyFactory();
        SortStrategy sortStrategy = sortStrategyFactory.createStrategy(systemDto.getDestination());
        return sortStrategy.sort(systemDto.getSourceMails());
    }
}