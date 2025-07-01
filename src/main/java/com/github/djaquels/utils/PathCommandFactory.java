package com.github.djaquels.utils;

import com.github.djaquels.utils.UserPathCommand;
import com.github.djaquels.utils.UserRemotePathCommand;
import com.github.djaquels.utils.GlobalPathCommand;
import com.github.djaquels.utils.GlobalRemotePathCommand;

public class PathCommandFactory {
    public static PathCommand createLocalPathCommand() {
        return new UserPathCommand();
    }
    public static PathCommand createUserRemotePathCommand(String username, String host,int port, String password) {
        return new UserRemotePathCommand(username, host, port, password);
    }
    public static PathCommand createGlobalPathCommand(){
        return new GlobalPathCommand();
    }
    public static PathCommand createGlobalRemotePathCommand(String username, String host, int port, String password) {
        return new GlobalRemotePathCommand(username, host, port, password);
    }
}