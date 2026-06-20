package com.daxi.limit;

public class ChatLimit {
    public static final int SESSION_STATUS_WAITING = 0;
    public static final int SESSION_STATUS_ACTIVE = 1;
    public static final int SESSION_STATUS_CLOSED = 2;

    public static final int SENDER_TYPE_USER = 0;
    public static final int SENDER_TYPE_AGENT = 1;

    public static final int AGENT_STATUS_OFFLINE = 0;
    public static final int AGENT_STATUS_ONLINE = 1;
    public static final int AGENT_STATUS_BUSY = 2;

    public static final int MAX_MESSAGE_LENGTH = 2000;

    public static final String AGENT_TOKEN_PREFIX = "agent:login:%s";
    public static final String JWT_ROLE_AGENT = "AGENT";
    public static final String JWT_ROLE_USER = "USER";
}
