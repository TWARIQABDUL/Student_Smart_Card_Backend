package com.smartaccess.backend.dto;

public enum Roles {
    MEMBER,         // General User
    ORG_ADMIN,      // Manager
    SUPER_ADMIN,    // SaaS Owner
    STAFF,          // Employees (Receptionist, Trainer, Bartender)
    GUARD,          // 👈 Security (The person scanning)
    GUEST           // Temporary
}