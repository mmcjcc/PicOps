/*
 * User.java
 *
 * 2005
 * This class is a POJO  (Plain Old Java Object) which will be used to represent users
 *
 */
package com.ezjcc.picops;

import java.util.Date;

/**
 *
 * @author Jason
 */
public class User {
    private String id;
    private String nameLast;
    private String nameFirst;
    private String userName;
    private String email;
    private String password;
    private int question;
    private String answer;
    private boolean validated;
    private Date registrationdate;
    /**
     * Creates a new instance of User
     */
    public User() {
    }
    /* ID */
    public String getId() {
        return id;
    }
    public void setId(String id){
        this.id = id;
    }
    /* Last Name */
    public String getNameLast() {
        return nameLast;
    }
    public void setNameLast(String nameLast){
        this.nameLast = nameLast;
    }
    /*First Name */
    public String getNameFirst() {
        return nameFirst;
    }
    public void setNameFirst(String nameFirst){
        this.nameFirst = nameFirst;
    }
    /*User Name */
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
    /* Email */
    public String getEmail() {
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    /* password */
    public String getPassword() {
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    /* Question ID */
    public int getQuestion() {
        return question;
    }
    public void setQuestion(int question){
        this.question = question;
    }
    /* Question Answer */
    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer){
        this.answer = answer;
    }
    /* User Validated? */
    public boolean getValidated() {
        return validated;
    }
    public void setValidated(boolean validated){
        this.validated = validated;
    }
    /* signup date */
    public Date getRegistrationdate() {
        return registrationdate; 
               
    }
    public void setRegistrationdate(Date registrationdate){
        this.registrationdate = registrationdate;
    }
}
