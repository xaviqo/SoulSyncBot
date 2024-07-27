package tech.xavi.soulsync.entity;

public enum Role {
    DEMO,
    ADMIN;

    public String getWithPrefix() {
        return "ROLE_" + this.name();
    }
}
