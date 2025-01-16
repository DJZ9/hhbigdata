package com.hhbigdata.worker.actor;

import com.hhbigdata.common.command.ExecuteCmdCommand;
import com.hhbigdata.common.utils.ExecResult;
import com.hhbigdata.common.utils.ShellUtils;

import akka.actor.UntypedActor;

public class RMStateActor extends UntypedActor {
    
    @Override
    public void onReceive(Object msg) throws Throwable {
        if (msg instanceof ExecuteCmdCommand) {
            ExecuteCmdCommand command = (ExecuteCmdCommand) msg;
            ExecResult execResult = ShellUtils.exceShell(command.getCommandLine());
            getSender().tell(execResult, getSelf());
        } else {
            unhandled(msg);
        }
    }
}
