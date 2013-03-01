package net.endercraftbuild.bungee.handlers;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.endercraftbuild.bungee.Vote4Diamondz;
import net.endercraftbuild.bungee.database.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.alta189.simplesave.query.OrderQuery;

public class VoteHandler extends AbstractHandler {

    private final Vote4Diamondz plugin;

	public VoteHandler(Vote4Diamondz plugin) {
    	this.plugin = plugin;
	}

	@Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
			// extract the url
			String url = request.getRequestURI();
			// push the vote path onto the stack
			String vote = "/vote/";
			// default response
			byte[] resp = plugin.getVoterHtml();
			// top votes
			if (url.startsWith("/top")) {
			    // start select
			    OrderQuery<VoteEntry> query = plugin.getDatabase().select(VoteEntry.class).order();
			    // build query
			    query.getPairs().add(new OrderQuery.OrderPair("voteCount", OrderQuery.Order.DESC));
			    // get results
			    List<VoteEntry> top = query.execute().find();
			    // build output
			    StringBuilder out = new StringBuilder();
			    // loop entries
			    for (VoteEntry entry : top) {
			        out.append("<li>");
			        out.append(entry.getName());
			        out.append(" - ");
			        out.append(entry.getVoteCount());
			        out.append(" vote");
			        if (entry.getVoteCount() > 1) {
			            out.append("s");
			        }
			        out.append("</li>");
			    }
			    // set output
			    resp = out.toString().getBytes();
			    // see if someone is voting
			} else if (url.startsWith(vote)) {
			    // extract everything after the vote path
			    String user = url.substring(vote.length());
			    // grab the player whom is trying to vote
			    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(user);
			    // store web ip
			    String ip = request.getRemoteAddr();
			    // check they are online
			    if (player == null) {
			        // send not online error
			        resp = plugin.getNotOnlineMessage();
			    } // check they come from the same ip
			    else if (plugin.shouldCheckIP() && !player.getAddress().getAddress().getHostAddress().equals(ip)) {
			    	ProxyServer.getInstance().getLogger().info(String.format("Player: %s / Web IP: %s / Game IP: %s", user, ip, player.getAddress().getAddress().getHostAddress()));
			        // send wrong ip error
			        resp = plugin.getWrongIPMessage();
			    } else {
			        // select old entry
			        VoteEntry entry = plugin.getEntry(user);
			        // check time has passed
			        if (!plugin.canVote(entry)) {
			            // set time error
			            resp = plugin.getTooSoonMessage();
			        } else {
			            // they have voted
			            // create new entry if they need it
			            if (entry == null) {
			                entry = new VoteEntry(user);
			            }
			            // update it with new info
			            entry.setLastVote(Vote4Diamondz.getTime());
			            entry.setVoteCount(entry.getVoteCount() + 1);
			            // save updated entry
			            plugin.getDatabase().save(entry);
			            // dispatch their reward
			            player.getServer().sendData(Vote4Diamondz.CHANNEL, player.getName().getBytes());
			            // log the request
			            VoteHistory log = new VoteHistory(user, ip);
			            plugin.getDatabase().save(log);
			            // set thanks message
			            resp = plugin.getThanksMessage();
			        }
			    }
			}
			// send the response
			// it will be html
			response.setContentType("text/html");
			// content length
			response.setContentLength(resp.length);
			// send it
			response.getOutputStream().write(resp);
			// clean up
			response.getOutputStream().close();
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().log(Level.WARNING, "Failed to process web request: ", e);
		}
    }
	
}
