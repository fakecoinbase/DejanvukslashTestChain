package com.chain.api.core.Net;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Checks every second for dead peers and mining tasks that need to be removed from the list
 */
public class MaintenanceThread implements Runnable {

    private List<CNode> vNodes;

    private List<MiningTask> miningTaskList;

    @Autowired
    public void setvNodes(List<CNode> vNodes) {
        this.vNodes = vNodes;
    }

    @Autowired
    public void setMiningTaskList(List<MiningTask> miningTaskList) {this.miningTaskList = miningTaskList;}


    @Override
    public void run() {
        while(true) {
            // check for dead peers
            for(int i = 0; i < vNodes.size(); i++) {
                if(vNodes.get(i).getSocket().isClosed()) {
                    vNodes.remove(i);
                    i--;
                }
            }

            // check for finished mining tasks
            for(int i = 0; i < miningTaskList.size(); i++) {
                if(!miningTaskList.get(i).getThread().isAlive()) {
                    miningTaskList.remove(i);
                    i--;
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
