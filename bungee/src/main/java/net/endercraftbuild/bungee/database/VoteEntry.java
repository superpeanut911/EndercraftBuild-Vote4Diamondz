package net.endercraftbuild.bungee.database;

import com.alta189.simplesave.Field;
import com.alta189.simplesave.Id;
import com.alta189.simplesave.Table;

@Table("votes")
public class VoteEntry {

    @Id
    private int id;
    
    @Field
    private String name;
    
    @Field
    private int voteCount;
    
    @Field
    private int lastVote;

    public VoteEntry() {
    }

    public VoteEntry(String name) {
        this.name = name;
    }
    
    public int getId() {
    	return id;
    }
    
    public String getName() {
    	return name;
    }
    
    public int getVoteCount() {
    	return voteCount;
    }
    
    public void setVoteCount(int voteCount) {
    	this.voteCount = voteCount;
    }
    
    public int getLastVote() {
    	return lastVote;
    }
    
    public void setLastVote(int lastVote) {
    	this.lastVote = lastVote;
    }

}
