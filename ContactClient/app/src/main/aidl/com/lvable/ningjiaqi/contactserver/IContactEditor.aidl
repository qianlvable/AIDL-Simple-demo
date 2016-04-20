// IContactEditor.aidl
package com.lvable.ningjiaqi.contactserver;

// Declare any non-default types here with import statements

interface IContactEditor {
    void addContact(String name, String phone);
    void updateContact(String name, String number, String contactId);
    void delteContact(String contactId);
    List<String> getAllConactIds();
}
