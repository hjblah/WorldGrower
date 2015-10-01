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
package org.worldgrower.gui;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.worldgrower.Constants;
import org.worldgrower.DungeonMaster;
import org.worldgrower.Main;
import org.worldgrower.ManagedOperation;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.actions.Actions;
import org.worldgrower.actions.BuildAction;
import org.worldgrower.actions.CraftAction;
import org.worldgrower.actions.magic.MagicSpell;
import org.worldgrower.actions.magic.ResearchSpellAction;
import org.worldgrower.conversation.Conversations;
import org.worldgrower.gui.chooseworldobject.ChooseWorldObjectAction;
import org.worldgrower.gui.chooseworldobject.GuiDisguiseAction;
import org.worldgrower.gui.chooseworldobject.GuiVoteAction;
import org.worldgrower.gui.conversation.GuiAskQuestionAction;
import org.worldgrower.gui.debug.GuiShowCommonersOverviewAction;
import org.worldgrower.gui.debug.GuiShowEconomicOverviewAction;
import org.worldgrower.gui.debug.GuiShowPropertiesAction;
import org.worldgrower.gui.debug.GuiShowSkillOverviewAction;
import org.worldgrower.gui.debug.ShowPerformedActionsAction;
import org.worldgrower.gui.inventory.GuiBuyAction;
import org.worldgrower.gui.inventory.GuiGetItemAction;
import org.worldgrower.gui.inventory.GuiPutItemAction;
import org.worldgrower.gui.inventory.GuiSellAction;
import org.worldgrower.gui.inventory.GuiStealAction;
import org.worldgrower.gui.inventory.InventoryAction;

public class GuiMouseListener extends MouseAdapter {
	private WorldPanel container;
	private WorldObject playerCharacter;
	private World world;
	private DungeonMaster dungeonMaster;
	private ImageInfoReader imageInfoReader;
	
	private final CharacterSheetAction characterSheetAction;
	private final InventoryAction inventoryAction;
	private final MagicOverviewAction magicOverviewAction;
	private final RestAction restAction;
	private final GuiShowOrganizationsAction createOrganizationAction;
	private final GuiAssignActionToLeftMouseAction assignActionToLeftMouseAction;
	private ManagedOperation leftMouseClickAction;
	
    public GuiMouseListener(WorldPanel container, WorldObject playerCharacter, World world, DungeonMaster dungeonMaster, ImageInfoReader imageInfoReader) {
		super();
		this.container = container;
		this.playerCharacter = playerCharacter;
		this.world = world;
		this.dungeonMaster = dungeonMaster;
		this.imageInfoReader = imageInfoReader;
		
		characterSheetAction = new CharacterSheetAction(playerCharacter, imageInfoReader);
		inventoryAction = new InventoryAction(playerCharacter, imageInfoReader, world, dungeonMaster, container);
		magicOverviewAction = new MagicOverviewAction(playerCharacter, imageInfoReader);
		restAction = new RestAction(playerCharacter, imageInfoReader, world, (WorldPanel)container, dungeonMaster);
		createOrganizationAction = new GuiShowOrganizationsAction(playerCharacter, world, container);
		assignActionToLeftMouseAction = getGuiAssignActionToLeftMouseAction();
		addKeyBindings();
	}

	private void addKeyBindings() {
		addKeyBindingsFor(characterSheetAction, "C");
		addKeyBindingsFor(inventoryAction, "I");
		addKeyBindingsFor(magicOverviewAction, "M");
		addKeyBindingsFor(restAction, "R");
		addKeyBindingsFor(createOrganizationAction, "O");
		addKeyBindingsFor(assignActionToLeftMouseAction, "A");
	}
	
	private void addKeyBindingsFor(Action action, String binding) {
		container.getInputMap().put(KeyStroke.getKeyStroke(binding), action.getClass().getSimpleName());
		container.getActionMap().put(action.getClass().getSimpleName(), action);
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(binding));
	}

	@Override
	public void mousePressed(MouseEvent e){
		mouseAction(e);
    }
 
    private void mouseAction(MouseEvent e) {
        int x = (int) e.getPoint().getX() / 48;
        int y = (int) e.getPoint().getY() / 48;

		WorldObject worldObject = ((WorldPanel)container).findWorldObject(x, y);
		
    	if (((WorldPanel)container).inBuildMode()) {
			if (SwingUtilities.isRightMouseButton(e)) {
				((WorldPanel)container).endBuildMode(false);
			} else {
				((WorldPanel)container).endBuildMode(true);
		    }
		} else {
	        if (SwingUtilities.isRightMouseButton(e)) {
	            doPop(e);
	        } else if (SwingUtilities.isLeftMouseButton(e) && isCtrlPressed(e) && worldObject != null) {
	        	performTalkAction(worldObject);
	        } else if (SwingUtilities.isLeftMouseButton(e) && leftMouseClickAction != null && worldObject != null) {
	        	performLeftMouseAction(worldObject);
	        } else {
	        	centerOnScreen(e);
	        }
		}
    }

	private boolean isCtrlPressed(MouseEvent evt) {
		return (evt.getModifiers() & InputEvent.CTRL_DOWN_MASK) == 0;
	}

    private void performLeftMouseAction(WorldObject worldObject) {
    	if (Main.canActionExecute(playerCharacter, leftMouseClickAction, new int[0], world, worldObject)) {
    		Main.executeAction(playerCharacter, leftMouseClickAction, new int[0], world, dungeonMaster, worldObject, container);
    	} else {
    		JOptionPane.showMessageDialog(container, "Cannot execute action '" + leftMouseClickAction.getSimpleDescription() + "' on " + worldObject.getProperty(Constants.NAME));
    	}
	}
    
    private void performTalkAction(WorldObject worldObject) {
    	if (canPlayerCharacterPerformTalkAction(worldObject, Actions.TALK_ACTION)) {
    		GuiAskQuestionAction guiAskQuestionAction = new GuiAskQuestionAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader);
    		guiAskQuestionAction.actionPerformed(null);
		}
    }

	private void centerOnScreen(MouseEvent e) {
    	int x = (int) e.getPoint().getX() / 48;
        int y = (int) e.getPoint().getY() / 48;
        
        ((WorldPanel)container).centerOffsetsOn(x, y);
        container.repaint();
	}

	private void doPop(MouseEvent e){
    	JPopupMenu menu = new JPopupMenu();
        int x = (int) e.getPoint().getX() / 48;
        int y = (int) e.getPoint().getY() / 48;

		WorldObject worldObject = ((WorldPanel)container).findWorldObject(x, y);
		
        if (worldObject != null) {
            if (worldObject.getProperty(Constants.ID) == 0) {
            	addPlayerCharacterInformationMenus(menu);
            	
            	JMenu organizationMenu = new JMenu("Organization");
            	JMenu miscMenu = new JMenu("Miscellaneous");
            	
            	addDisguiseMenu(miscMenu);
            	
            	addPropertiesMenu(menu, playerCharacter);
            	addBuildActions(menu);
            	addCraftActions(menu);
            	addWeaveActions(menu);
            	addBrewActions(menu);
            	addPlantActions(menu);
            	addIllusionActions(menu);
            	addRestorationActions(menu);
            	addTransmutationActions(menu);
            	addScribeMagicSpells(menu);
            	addRestMenu(menu);
            	menu.add(organizationMenu);
            	addCreateOrganizationMenu(organizationMenu);
            	addShowLegalActionsMenu(organizationMenu);
            	addShowOrganizationsActionMenu(organizationMenu);
            	addChooseDeityMenu(miscMenu);
            	addCreateHumanMeatMenu(miscMenu);
            	menu.add(miscMenu);
            	addAssignActionsToLeftMouse(menu);
            	
            	menu.show(e.getComponent(), e.getX(), e.getY());
            } else {
            	if (worldObject.hasIntelligence()) {
            		addCommunicationActions(menu, worldObject);
            	} else {
            		addInventoryActions(menu, worldObject);
            		addVoteActions(menu, worldObject);
            		addResearchActions(menu, worldObject);
            	}
            	addAllActions(menu, worldObject);
            	
            	addPropertiesMenu(menu, worldObject);
            	addPerformedActionsMenu(menu, worldObject);
            	
            	menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

	private void addDisguiseMenu(JMenu menu) {
		JMenuItem disguiseMenuItem = new JMenuItem(new GuiDisguiseAction(playerCharacter, imageInfoReader, world, (WorldPanel)container, dungeonMaster, Actions.DISGUISE_ACTION));
		disguiseMenuItem.setText("Disguise...");
		menu.add(disguiseMenuItem);
	}

	private void addChooseDeityMenu(JMenu menu) {
		JMenuItem chooseDeityMenuItem = new JMenuItem(new ChooseDeityAction(playerCharacter, imageInfoReader, world, (WorldPanel)container, dungeonMaster));
		chooseDeityMenuItem.setText("Choose Deity...");
		menu.add(chooseDeityMenuItem);
	}

	private void addCreateHumanMeatMenu(JMenu menu) {
		ManagedOperation[] actions = { Actions.CREATE_HUMAN_MEAT_ACTION };
		addActions(menu, actions);
	}
	
	private void addShowLegalActionsMenu(JMenu menu) {
		JMenuItem showLegalActionsMenuItem = new JMenuItem(new GuiShowLegalActionsAction(playerCharacter, dungeonMaster, world, container));
		showLegalActionsMenuItem.setText("Show legal actions...");
		menu.add(showLegalActionsMenuItem);
	}

	private void addShowOrganizationsActionMenu(JMenu menu) {
		JMenuItem showOrganizationsMenuItem = new JMenuItem(createOrganizationAction);
		showOrganizationsMenuItem.setText("Organization Membership Overview");
		menu.add(showOrganizationsMenuItem);
	}
	
	private void addCreateOrganizationMenu(JMenu menu) {
		JMenuItem createOrganizationMenuItem = new JMenuItem(new GuiCreateOrganizationAction(playerCharacter, imageInfoReader, world, (WorldPanel)container, dungeonMaster));
		createOrganizationMenuItem.setText("Create Organization...");
		menu.add(createOrganizationMenuItem);
	}

	private void addRestMenu(JPopupMenu menu) {
		JMenuItem restMenuItem = new JMenuItem(restAction);
		setMenuIcon(restMenuItem, ImageIds.SLEEPING_INDICATOR);
		restMenuItem.setText("Rest...");
		menu.add(restMenuItem);
	}

	private void setMenuIcon(JMenuItem menuItem, ImageIds imageIds) {
		menuItem.setIcon(new ImageIcon(imageInfoReader.getImage(imageIds, null)));
	}

	private void addCommunicationActions(JPopupMenu menu, WorldObject worldObject) {
		if (canPlayerCharacterPerformTalkAction(worldObject, Actions.TALK_ACTION)) {
			JMenuItem guiTalkMenuItem = new JMenuItem(new GuiAskQuestionAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader));
			guiTalkMenuItem.setText("Talk...");
			menu.add(guiTalkMenuItem);
		}
		
		if (canPlayerCharacterPerformAction(worldObject, Actions.SELL_ACTION)) {
			JMenuItem guiSellMenuItem = new JMenuItem(new GuiSellAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader));
			guiSellMenuItem.setText("Sell...");
			menu.add(guiSellMenuItem);
		}
		
		if (canPlayerCharacterPerformAction(worldObject, Actions.BUY_ACTION)) {
			JMenuItem guiBuyMenuItem = new JMenuItem(new GuiBuyAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader));
			guiBuyMenuItem.setText("Buy...");
			menu.add(guiBuyMenuItem);
		}
		
		if (canPlayerCharacterPerformAction(worldObject, Actions.STEAL_ACTION)) {
			JMenuItem guiStealMenuItem = new JMenuItem(new GuiStealAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader));
			guiStealMenuItem.setText("Steal...");
			menu.add(guiStealMenuItem);
		}
	}
	
	private void addInventoryActions(JPopupMenu menu, WorldObject worldObject) {
		if (canPlayerCharacterPerformAction(worldObject, Actions.GET_ITEM_FROM_INVENTORY_ACTION)) {
			JMenuItem guiStealMenuItem = new JMenuItem(new GuiGetItemAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader));
			guiStealMenuItem.setText("Get Item...");
			menu.add(guiStealMenuItem);
		}
		
		if (canPlayerCharacterPerformAction(worldObject, Actions.PUT_ITEM_INTO_INVENTORY_ACTION)) {
			JMenuItem guiStealMenuItem = new JMenuItem(new GuiPutItemAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader));
			guiStealMenuItem.setText("Put Item...");
			menu.add(guiStealMenuItem);
		}
	}
	
	private void addVoteActions(JPopupMenu menu, WorldObject worldObject) {
		if (canPlayerCharacterPerformAction(worldObject, Actions.VOTE_FOR_LEADER_ACTION)) {
			JMenuItem guiVoteMenuItem = new JMenuItem(new GuiVoteAction(playerCharacter, imageInfoReader, world, container, dungeonMaster, worldObject));
			guiVoteMenuItem.setText("Vote...");
			menu.add(guiVoteMenuItem);
		}
	}
	
	private void addResearchActions(JPopupMenu menu, WorldObject worldObject) {
		if (ResearchSpellAction.isValidTarget(worldObject) && Actions.getMagicSpellsToResearch(playerCharacter).size() > 0) {
			JMenuItem guiResearchMagicSpellMenuItem = new JMenuItem(new GuiResearchMagicSpellAction(playerCharacter, imageInfoReader, world, container, dungeonMaster, worldObject));
			guiResearchMagicSpellMenuItem.setText("Research multiple turns...");
			menu.add(guiResearchMagicSpellMenuItem);
		}
	}

	private void addPlayerCharacterInformationMenus(JPopupMenu menu) {
		JMenuItem characterSheetMenuItem = new JMenuItem(characterSheetAction);
		characterSheetMenuItem.setText("Character Sheet");
		menu.add(characterSheetMenuItem);
		
		JMenuItem inventoryMenuItem = new JMenuItem(inventoryAction);
		inventoryMenuItem.setText("Inventory");
		menu.add(inventoryMenuItem);
		
		JMenuItem magicOverviewMenuItem = new JMenuItem(magicOverviewAction);
		magicOverviewMenuItem.setText("Magic Overview");
		menu.add(magicOverviewMenuItem);
	}

	private void addBuildActions(JPopupMenu menu) {
		BuildAction[] buildActions = { Actions.BUILD_SHACK_ACTION, Actions.BUILD_HOUSE_ACTION, Actions.BUILD_SHRINE_ACTION, Actions.BUILD_SMITH_ACTION, Actions.BUILD_WELL_ACTION, Actions.BUILD_PAPER_MILL_ACTION, Actions.BUILD_LIBRARY_ACTION, Actions.CREATE_GRAVE_ACTION, Actions.CONSTRUCT_TRAINING_DUMMY_ACTION, Actions.BUILD_JAIL_ACTION, Actions.BUILD_SACRIFICAL_ALTAR_ACTION, Actions.BUILD_ARENA_ACTION };
		addBuildActions(menu, "Build", buildActions);
	}
	
	private void addPlantActions(JPopupMenu menu) {
		BuildAction[] buildActions = { Actions.PLANT_BERRY_BUSH_ACTION, Actions.PLANT_GRAPE_VINE_ACTION, Actions.PLANT_TREE_ACTION, Actions.PLANT_COTTON_PLANT_ACTION, Actions.PLANT_NIGHT_SHADE_ACTION };
		addBuildActions(menu, "Plant", buildActions);
	}
	
	private void addIllusionActions(JPopupMenu menu) {
		BuildAction[] buildActions = { Actions.MINOR_ILLUSION_ACTION };
		MagicSpell[] illusionActions = { Actions.INVISIBILITY_ACTION };
		JMenu illusionMenu = addBuildActions(menu, "Illusions", buildActions, buildAction -> new ChooseWorldObjectAction(playerCharacter, imageInfoReader, world, ((WorldPanel)container), dungeonMaster, new StartBuildModeAction(playerCharacter, imageInfoReader, ((WorldPanel)container), buildAction)));
		addActions(illusionMenu, illusionActions);
		
    	JMenuItem disguiseMenuItem = new JMenuItem(new GuiDisguiseAction(playerCharacter, imageInfoReader, world, (WorldPanel)container, dungeonMaster, Actions.DISGUISE_MAGIC_SPELL_ACTION));
    	disguiseMenuItem.setText("Disguise self");
    	disguiseMenuItem.setToolTipText(Actions.DISGUISE_MAGIC_SPELL_ACTION.getRequirementsDescription());
    	illusionMenu.add(disguiseMenuItem);
	}
	
	private void addRestorationActions(JPopupMenu menu) {
		MagicSpell[] restorationActions = { Actions.MINOR_HEAL_ACTION, Actions.CURE_DISEASE_ACTION, Actions.CURE_POISON_ACTION };
		addActions(menu, "Restoration", restorationActions);
	}
	
	private void addTransmutationActions(JPopupMenu menu) {
		MagicSpell[] transmutationActions = { Actions.ENLARGE_ACTION, Actions.REDUCE_ACTION, Actions.SLEEP_MAGIC_SPELL_ACTION, Actions.WATER_WALK_ACTION, Actions.BURDEN_ACTION, Actions.FEATHER_ACTION };
		addActions(menu, "Transmute", transmutationActions);
	}
	
	private void addScribeMagicSpells(JPopupMenu menu) {
		ManagedOperation[] scribeActions = Actions.getAllScribeMagicSpellActions().toArray(new ManagedOperation[0]);
		addActions(menu, "Scribe spells", scribeActions);
	}
	
	private void addBuildActions(JPopupMenu menu, String menuTitle, BuildAction[] buildActions) {
		addBuildActions(menu, menuTitle, buildActions, buildAction -> new StartBuildModeAction(playerCharacter, imageInfoReader, ((WorldPanel)container), buildAction));
	}
	
	private JMenu addBuildActions(JPopupMenu menu, String menuTitle, BuildAction[] buildActions, Function<BuildAction, Action> guiActionBuilder) {
		JMenu parentMenuItem = new JMenu(menuTitle);
		menu.add(parentMenuItem);
		
		
		for(BuildAction buildAction : buildActions) {
			final JMenuItem buildMenuItem;
			if (canPlayerCharacterPerformBuildAction(buildAction)) {
				buildMenuItem = new JMenuItem(guiActionBuilder.apply(buildAction));
				buildMenuItem.setText(buildAction.getDescription(playerCharacter, null, null, world) + "...");
				parentMenuItem.add(buildMenuItem);
			} else {
				buildMenuItem = createDisabledActionMenuItem(parentMenuItem, buildAction);
			}
			buildMenuItem.setToolTipText(buildAction.getRequirementsDescription());
		}
		return parentMenuItem;
	}
	
	private void addCraftActions(JPopupMenu menu) {
		CraftAction[] craftActions = { Actions.CRAFT_IRON_CLAYMORE_ACTION, Actions.CRAFT_IRON_CUIRASS_ACTION, Actions.CRAFT_IRON_HELMET_ACTION, Actions.CRAFT_IRON_GAUNTLETS_ACTION, Actions.CRAFT_IRON_GREAVES_ACTION, Actions.CRAFT_IRON_BOOTS_ACTION, Actions.CRAFT_IRON_SHIELD_ACTION, Actions.CRAFT_IRON_GREATSWORD_ACTION, Actions.CRAFT_IRON_AXE_ACTION, Actions.CRAFT_IRON_GREATAXE_ACTION, Actions.CRAFT_LONG_BOW_ACTION, Actions.MINT_GOLD_ACTION, Actions.CREATE_PAPER_ACTION, Actions.CONSTRUCT_BED_ACTION, Actions.CONSTRUCT_FISHING_POLE_ACTION, Actions.CRAFT_REPAIR_HAMMER_ACTION };
		addActions(menu, "Craft", craftActions);
	}
	
	private void addWeaveActions(JPopupMenu menu) {
		CraftAction[] weaveActions = { Actions.WEAVE_COTTON_SHIRT_ACTION, Actions.WEAVE_COTTON_HAT_ACTION, Actions.WEAVE_COTTON_BOOTS_ACTION, Actions.WEAVE_COTTON_GLOVES_ACTION, Actions.WEAVE_COTTON_PANTS_ACTION };
		addActions(menu, "Weave", weaveActions);
	}

	private void addBrewActions(JPopupMenu menu) {
		CraftAction[] brewActions = { Actions.BREW_WINE_ACTION, Actions.BREW_POISON_ACTION };
		addActions(menu, "Brew", brewActions);
	}
	
	private void addActions(JPopupMenu menu, String menuTitle, ManagedOperation[] actions) {
		JMenu parentMenuItem = new JMenu(menuTitle);
		menu.add(parentMenuItem);
		
		addActions(parentMenuItem, actions);
	}

	private void addActions(JMenu parentMenuItem, ManagedOperation[] actions) {
		for(ManagedOperation action : actions) {
			final JMenuItem menuItem;
			if (canPlayerCharacterPerformBuildAction(action)) {
				PlayerCharacterAction guiAction = new PlayerCharacterAction(playerCharacter, world, container, dungeonMaster, action, playerCharacter);
				menuItem = new JMenuItem(guiAction);
				menuItem.setText(action.getDescription(playerCharacter, playerCharacter, null, world) + "...");
				parentMenuItem.add(menuItem);
			} else {
				menuItem = createDisabledActionMenuItem(parentMenuItem, action);
			}
			addToolTips(action, menuItem);
			addImageIcon(action, menuItem);
		}
	}

	private void addImageIcon(ManagedOperation action, JMenuItem menuItem) {
		if (menuItem != null) {
			if (action instanceof MagicSpell) {
				MagicSpell magicSpell = (MagicSpell) action;
				setMenuIcon(menuItem, magicSpell.getImageIds());
			}
		}
	}

	private void addToolTips(ManagedOperation action, final JMenuItem menuItem) {
		if (menuItem != null) {
			menuItem.setToolTipText(action.getRequirementsDescription());
		}
	}

	private JMenuItem createDisabledActionMenuItem(JMenuItem menu, ManagedOperation craftAction) {
		JMenuItem menuItem = new JMenuItem(craftAction.getDescription(playerCharacter, playerCharacter, null, world) + "...");
		menuItem.setEnabled(false);
		menu.add(menuItem);
		return menuItem;
	}

	private void addAllActions(JPopupMenu menu, WorldObject worldObject) {
		for(ManagedOperation action : playerCharacter.getOperations()) {
			if (action.getArgumentRanges().length == 0) {
				final JMenuItem menuItem;
				if (canPlayerCharacterPerformAction(worldObject, action)) {
					PlayerCharacterAction guiAction = new PlayerCharacterAction(playerCharacter, world, container, dungeonMaster, action, worldObject);
					menuItem = new JMenuItem(guiAction);
					menuItem.setText(action.getSimpleDescription());
					menu.add(menuItem);
				} else if (canPlayerCharacterPerformActionUnderCorrectCircumstances(worldObject, action)) {
					menuItem = new JMenuItem(action.getSimpleDescription());
					menuItem.setEnabled(false);
					menu.add(menuItem);
				} else {
					menuItem = null;
				}
				addToolTips(action, menuItem);
				addImageIcon(action, menuItem);
			}
		}
	}
	
	private List<ManagedOperation> getActions() {
		List<ManagedOperation> actions = new ArrayList<>();
		for(ManagedOperation action : playerCharacter.getOperations()) {
			if (action.getArgumentRanges().length == 0 && !(action instanceof BuildAction) && !(Actions.getInventoryActions().contains(action))) {
				actions.add(action);
			}
		}
		Actions.sortActionsByDescription(actions);
		return actions;
	}
	
	private void addAssignActionsToLeftMouse(JPopupMenu menu) {
		JMenuItem guiAssignActionsToLeftMouseItem = new JMenuItem(assignActionToLeftMouseAction);
		guiAssignActionsToLeftMouseItem.setText("Assign action to left mouse click...");
		menu.add(guiAssignActionsToLeftMouseItem);
	}

	private GuiAssignActionToLeftMouseAction getGuiAssignActionToLeftMouseAction() {
		return new GuiAssignActionToLeftMouseAction(getActions(), container, this);
	}

	private void addPropertiesMenu(JPopupMenu menu, WorldObject worldObject) {
		if (Boolean.getBoolean("DEBUG")) {
			JMenuItem guiPropertiesItem = new JMenuItem(new GuiShowPropertiesAction(worldObject));
			guiPropertiesItem.setText("Properties...");
			menu.add(guiPropertiesItem);
			
			JMenuItem guiShowCommonersOverviewItem = new JMenuItem(new GuiShowCommonersOverviewAction(world));
			guiShowCommonersOverviewItem.setText("Show Commoners Overview...");
			menu.add(guiShowCommonersOverviewItem);
			
			JMenuItem guiShowEconomicOverviewItem = new JMenuItem(new GuiShowEconomicOverviewAction(world));
			guiShowEconomicOverviewItem.setText("Show Economic Overview...");
			menu.add(guiShowEconomicOverviewItem);
			
			JMenuItem guiShowSkillOverviewItem = new JMenuItem(new GuiShowSkillOverviewAction(world));
			guiShowSkillOverviewItem.setText("Show Skill Overview...");
			menu.add(guiShowSkillOverviewItem);			
		}
	}
	
	private void addPerformedActionsMenu(JPopupMenu menu, WorldObject worldObject) {
		if (Boolean.getBoolean("DEBUG")) {
			JMenuItem showPerformedActionsItem = new JMenuItem(new ShowPerformedActionsAction(worldObject, world));
			showPerformedActionsItem.setText("Show performed actions...");
			menu.add(showPerformedActionsItem);
		}
	}

	private boolean canPlayerCharacterPerformAction(WorldObject worldObject, ManagedOperation action) {
		return canPlayerCharacterPerformActionUnderCorrectCircumstances(worldObject, action) && action.isActionPossible(playerCharacter, worldObject, new int[0], world);
	}
	private boolean canPlayerCharacterPerformTalkAction(WorldObject worldObject, ManagedOperation action) {
		return canPlayerCharacterPerformActionUnderCorrectCircumstances(worldObject, action) && action.isActionPossible(playerCharacter, worldObject, Conversations.createArgs(Conversations.NAME_CONVERSATION), world);
	}
	
	private boolean canPlayerCharacterPerformActionUnderCorrectCircumstances(WorldObject worldObject, ManagedOperation action) {
		return action.isValidTarget(playerCharacter, worldObject, world) && playerCharacter.canWorldObjectPerformAction(action);
	}
	
	private boolean canPlayerCharacterPerformBuildAction(ManagedOperation action) {
		return action.isActionPossible(playerCharacter, playerCharacter, new int[0], world) && playerCharacter.canWorldObjectPerformAction(action);
	}

	public void executeBuildAction(ManagedOperation buildAction, WorldObject buildLocation, int[] args) {
		Main.executeAction(playerCharacter, buildAction, args, world, dungeonMaster, buildLocation, container);
	}

	public void setLeftMouseClickAction(ManagedOperation action) {
		leftMouseClickAction = action;
		
	}
}