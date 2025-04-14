package com.example.btlandroid.Model;

import java.io.Serializable;

public class User implements Serializable {
        public String id;
        public String password;

        public String name;
        public String email;
        public String phone;

        public String role;

        public User(){
        }
        public User(String id,String password, String name, String email, String phone, String role) {
            this.id = id;
            this.password = password;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
        }

        public String getId(){
            return id;
        }

        public void setId(String id){
            this.id = id;
        }

        public String getPassword(){
            return password;
        }

        public void setPassword(String password){
            this.password = password;
        }

        public String getName(){
            return name;
        }

        public void setName(String name){
            this.name = name;
        }

        public String getEmail(){
            return email;
        }

        public void setEmail(String email){
            this.email = email;
        }

        public String getPhone(){
            return phone;
        }

        public String getRole(){return role;}
        public void setRole(String role){
            this.role = role;
        }
        public void setPhone(String phone){
            this.phone = phone;
        }

}
