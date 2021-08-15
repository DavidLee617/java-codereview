package com.company.juc.jucDesign.activeobject;

public class ActiveDaemonThread extends Thread {
    private ActiveMessageQueue activeQueue;
    private ActiveMessageQueue activeMessageQueue;
    public ActiveDaemonThread(ActiveMessageQueue activeQueue){
        super("ActiveDeamonThread");
        this.activeQueue=activeQueue;
        setDaemon(true);
    }

	@Override
    public void run(){
        while(true){
            MethodMessage methodMessage=this.activeQueue.take();
            methodMessage.execute();
        }
    }
}
