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
import org.worldgrower.OperationInfo;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.actions.Actions;
import org.worldgrower.goal.BrawlPropertyUtils;
import org.worldgrower.goal.GoalUtils;
import org.worldgrower.goal.Goals;
import org.worldgrower.goal.RelationshipPropertyUtils;
import org.worldgrower.history.HistoryItem;
import org.worldgrower.text.TextId;

public class BrawlConversation implements Conversation {

	private static final int YES = 0;
	private static final int NO = 1;
	private static final int LATER = 2;
	private static final int NOT_ENOUGH_GOLD = 3;
	private static final int GET_LOST = 4;
	
	@Override
	public Response getReplyPhrase(ConversationContext conversationContext) {
		final int replyId;
		
		WorldObject performer = conversationContext.getPerformer();
		WorldObject target = conversationContext.getTarget();
		World world = conversationContext.getWorld();
		int brawlStakeGold = conversationContext.getAdditionalValue();
		int relationshipValue = target.getProperty(Constants.RELATIONSHIPS).getValue(performer);

		if (relationshipValue < -900) {
			replyId = GET_LOST;
		} else if (target.getProperty(Constants.GOLD) < brawlStakeGold) {
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
			questions.add(new Question(gold, TextId.QUESTION_BRAWL_GOLD, gold, GOLD));
		}
		
		questions.add(new Question(0, TextId.QUESTION_BRAWL));
		
		return questions;
	}
	
	@Override
	public List<Response> getReplyPhrases(ConversationContext conversationContext) {
		WorldObject target = conversationContext.getTarget();
		World world = conversationContext.getWorld();
		
		OperationInfo immediateGoal = world.getImmediateGoal(target, world);
		final String immediateGoalDescription;
		if (immediateGoal != null) {
			immediateGoalDescription = immediateGoal.getDescription(world);
		} else {
			immediateGoalDescription = "resting";
		}
		
		return Arrays.asList(
			new Response(YES, TextId.ANSWER_BRAWL_YES),
			new Response(NO, TextId.ANSWER_BRAWL_NO),
			new Response(LATER, TextId.ANSWER_BRAWL_LATER, immediateGoalDescription),
			new Response(NOT_ENOUGH_GOLD, TextId.ANSWER_BRAWL_NOT_ENOUGH_GOLD),
			new Response(GET_LOST, TextId.ANSWER_BRAWL_GET_LOST)
			);
	}

	@Override
	public boolean isConversationAvailable(WorldObject performer, WorldObject target, WorldObject subject, World world) {
		return (!BrawlPropertyUtils.isBrawling(performer)) && (!BrawlPropertyUtils.isBrawling(target));
	}

	@Override
	public void handleResponse(int replyIndex, ConversationContext conversationContext) {
		WorldObject performer = conversationContext.getPerformer();
		WorldObject target = conversationContext.getTarget();
		World world = conversationContext.getWorld();
		
		if (replyIndex == YES) {
			int brawlStakeGold = conversationContext.getAdditionalValue();
			BrawlPropertyUtils.startBrawl(performer, target, brawlStakeGold);
		} else if (replyIndex == GET_LOST) {
			RelationshipPropertyUtils.changeRelationshipValue(performer, target, -100, Actions.TALK_ACTION, Conversations.createArgs(this), world);
		}
	}
	
	@Override
	public String getDescription(WorldObject performer, WorldObject target, World world) {
		return "talking about starting a brawl";
	}
}
