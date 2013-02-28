package net.endercraftbuild.bungee.database;

import net.endercraftbuild.bungee.Vote4Diamondz;

import com.alta189.simplesave.Field;
import com.alta189.simplesave.Id;
import com.alta189.simplesave.Table;

@Table("history")
public class VoteHistory {
	
    @Id
    private int id;
    
    @Field
    private String user;
    
    @Field
    private String ip;
    
    @Field
    private int time;

    public VoteHistory() {
    }

    public VoteHistory(String user, String ip) {
        this.user = user;
        this.ip = ip;
        this.time = Vote4Diamondz.getTime();
    }

}
