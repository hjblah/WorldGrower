/*******************************************************************************
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.worldgrower.conversation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.worldgrower.Constants;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.goal.DrinkingContestPropertyUtils;
import org.worldgrower.goal.GoalUtils;
import org.worldgrower.goal.Goals;
import org.worldgrower.history.HistoryItem;

public class DrinkingContestConversation implements Conversation {

	private static final int YES = 0;
	private static final int NO = 1;
	private static final int LATER = 2;
	private static final int NOT_ENOUGH_GOLD = 3;
	
	@Override
	public Response getReplyPhrase(ConversationContext conversationContext) {
		final int replyId;
		
		WorldObject target = conversationContext.getTarget();
		World world = conversationContext.getWorld();
		int drinkingStakeGold = conversationContext.getAdditionalValue();

		if (target.getProperty(Constants.GOLD) < drinkingStakeGold) {
			replyId = NOT_ENOUGH_GOLD;
		} else if (GoalUtils.currentGoalHasLowerPriorityThan(Goals.REST_GOAL, target, world)) {
			replyId = YES;
		} else {
			replyId = LATER;
		}
		
		return getReply(getReplyPhrases(conversationContext), replyId);
	}

	@Override
	public List<Question> getQuestionPhrases(WorldObject performer, WorldObject target, HistoryItem questionHistoryItem, WorldObject subjectWorldObject, World world) {
		List<Question> questions = new ArrayList<>();
		for(int gold = 20; gold < 100; gold += 20) {
			questions.add(new Question(null, "I want to have a drinking contest with you and I bet " + gold + " gold that I'm going to win. Do you accept?", gold));
		}
		
		questions.add(new Question(null, "I want to have a drinking contest with you. Do you accept?", 0));
		
		return questions;
	}
	
	@Override
	public List<Response> getReplyPhrases(ConversationContext conversationContext) {
		WorldObject target = conversationContext.getTarget();
		World world = conversationContext.getWorld();
		
		return Arrays.asList(
			new Response(YES, "Yes, the first one to become intoxicated or doesn't drink alcohol loses."),
			new Response(NO, "No"),
			new Response(LATER, "I'd love to, but I'm currently " + world.getImmediateGoal(target, world).getDescription(world)),
			new Response(NOT_ENOUGH_GOLD, "Not for the moment, I can't match your bet")
			);
	}

	@Override
	public boolean isConversationAvailable(WorldObject performer, WorldObject target, WorldObject subject, World world) {
		return (!DrinkingContestPropertyUtils.isDrinking(performer)) 
				&& (!DrinkingContestPropertyUtils.isDrinking(target))
				&& performer.getProperty(Constants.INVENTORY).getQuantityFor(Constants.ALCOHOL_LEVEL) > 5
				&& target.getProperty(Constants.INVENTORY).getQuantityFor(Constants.ALCOHOL_LEVEL) > 5;
	}

	@Override
	public void handleResponse(int replyIndex, ConversationContext conversationContext) {
		if (replyIndex == YES) {
			WorldObject performer = conversationContext.getPerformer();
			WorldObject target = conversationContext.getTarget();
			int dringStakeGold = conversationContext.getAdditionalValue();
			
			DrinkingContestPropertyUtils.startDrinkingContest(performer, target, dringStakeGold);
		}
	}
	
	@Override
	public String getDescription(WorldObject performer, WorldObject target, World world) {
		return "talking about starting a drinking contest";
	}
}
