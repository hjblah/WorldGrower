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
package org.worldgrower.actions;

import java.io.ObjectStreamException;

import org.worldgrower.Constants;
import org.worldgrower.ManagedOperation;
import org.worldgrower.Reach;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.attribute.WorldObjectContainer;
import org.worldgrower.goal.BuySellUtils;
import org.worldgrower.goal.InventoryPropertyUtils;
import org.worldgrower.gui.ImageIds;
import org.worldgrower.gui.music.SoundIds;

public class BuyAction implements ManagedOperation {

	@Override
	public void execute(WorldObject performer, WorldObject target, int[] args, World world) {
		int index = args[0];
		int price = args[1];
		int quantity = args[2];
		
		WorldObjectContainer performerInventory = performer.getProperty(Constants.INVENTORY);
		WorldObjectContainer targetInventory = target.getProperty(Constants.INVENTORY);
		
		int goldPaid = calculateGoldPaid(target, index, quantity);
		
		WorldObject boughtWorldObject = targetInventory.get(index).deepCopy();
		targetInventory.removeQuantity(index, quantity);
		
		boughtWorldObject.setProperty(Constants.SELLABLE, Boolean.FALSE);
		target.getProperty(Constants.ITEMS_SOLD).add(boughtWorldObject);
		
		performerInventory.addQuantity(boughtWorldObject, quantity);
		
		target.setProperty(Constants.GOLD, target.getProperty(Constants.GOLD) + goldPaid);
		performer.setProperty(Constants.GOLD, performer.getProperty(Constants.GOLD) - goldPaid);
		
		InventoryPropertyUtils.cleanupEquipmentSlots(target);
		
		String description = boughtWorldObject.getProperty(Constants.NAME);
		world.logAction(this, performer, target, args, performer.getProperty(Constants.NAME) + " bought " + quantity + " " + description + " at " + price + " gold a piece for a total of " + goldPaid + " gold");
	}

	int calculateGoldPaid(WorldObject target, int index, int quantity) {
		WorldObject worldObject = target.getProperty(Constants.INVENTORY).get(index);
		if (worldObject != null) {
			return BuySellUtils.getPrice(target, worldObject) * quantity;
		} else {
			return 0;
		}
	}

	@Override
	public boolean isActionPossible(WorldObject performer, WorldObject target, int[] args, World world) {
		int index = args[0];
		int price = args[1];
		int quantity = args[2];
		
		return canPerformerBuy(performer, target, index, quantity);
	}
	
	@Override
	public int distance(WorldObject performer, WorldObject target, int[] args, World world) {
		return Reach.evaluateTarget(performer, args, target, 1);
	}
	
	private boolean canPerformerBuy(WorldObject performer, WorldObject target, int index, int quantity) {
		final boolean canPerformerBuy;
		WorldObject worldObjectToBuy = target.getProperty(Constants.INVENTORY).get(index);
		if (worldObjectToBuy != null) {
			if (BuySellUtils.performerCanBuyGoods(performer, target, index, quantity)) {
				canPerformerBuy = true;
			} else {
				canPerformerBuy = false;
			}
		} else {
			canPerformerBuy = false;
		}
		return canPerformerBuy;
	}
	
	@Override
	public String getRequirementsDescription() {
		return CraftUtils.getRequirementsDescription(Constants.DISTANCE, 1);
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}

	@Override
	public boolean isValidTarget(WorldObject performer, WorldObject target, World world) {
		return (target.hasIntelligence() && target.hasProperty(Constants.INVENTORY) && target.getProperty(Constants.CREATURE_TYPE).canTrade());
	}
	
	@Override
	public String getDescription(WorldObject performer, WorldObject target, int[] args, World world) {
		return "buying from " + target.getProperty(Constants.NAME);
	}

	@Override
	public String getSimpleDescription() {
		return "buy";
	}
	
	public Object readResolve() throws ObjectStreamException {
		return readResolveImpl();
	}

	@Override
	public ImageIds getImageIds() {
		return ImageIds.GOLD_COIN;
	}
	
	@Override
	public SoundIds getSoundId() {
		return SoundIds.HANDLE_COINS;
	}
}