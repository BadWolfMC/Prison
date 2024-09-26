package tech.mcprison.prison.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.spigot.SpigotPrison;

public class PrisonUtilsModule
	extends Module 
{
	public static final String MODULE_NAME = "Utils";
	
	private int modulesEnabled = 0;
	
	private YamlConfiguration modulesConf;
    
    public PrisonUtilsModule(String version, YamlConfiguration modulesConf) {
        super(MODULE_NAME, version, 3);

        this.modulesConf = modulesConf;
    }


	@Override
	public String getBaseCommands() {
		return "/prison utils";
	}

	@Override
	public void enable() {
			
		int modulesEnabled = 0;
		
		if ( isEnabled( "utils.enabled", true ) ) {
			
			if ( isEnabled( "utils.repair.enabled", true ) ) {
			
				PrisonUtilsRepair utils = new PrisonUtilsRepair();
				
				utils.setEnableRepairAll( isEnabled( "utils.repair.repairAll", true ) );
				utils.setEnableRepairHand( isEnabled( "utils.repair.repairHand", true ) );
				
				Prison.get().getCommandHandler().registerCommands( utils );

				modulesEnabled++;
			}
			
			if ( isEnabled( "utils.messages.enabled", true ) ) {
				
				PrisonUtilsMessages utils = new PrisonUtilsMessages();
				
				utils.setEnableMessageMsg( isEnabled( "utils.messages.msg", true ) );
				utils.setEnableMessageBroadcast( isEnabled( "utils.messages.broadcast", true ) );
				
				Prison.get().getCommandHandler().registerCommands( utils );
				
				modulesEnabled++;
			}
			
			if ( isEnabled( "utils.mining.enabled", true ) ) {
				
				PrisonUtilsMining utils = new PrisonUtilsMining();
				
				utils.setEnableMiningSmelt( isEnabled( "utils.mining.smelt", true ) );
				utils.setEnableMiningBlock( isEnabled( "utils.mining.block", true ) );
				
				Prison.get().getCommandHandler().registerCommands( utils );
				
				modulesEnabled++;
			}

			if ( isEnabled( "utils.healing.enabled", true ) ) {
				PrisonUtilsHealing utils = new PrisonUtilsHealing();
				utils.setEnableHealingHeal( isEnabled( "utils.healing.heal", true ) );
				utils.setEnableHealingFeed( isEnabled( "utils.healing.feed", true ) );
				utils.setEnableHealingBreath( isEnabled("utils.healing.breath", true));

				Prison.get().getCommandHandler().registerCommands( utils );

				modulesEnabled++;
			}


			if ( isEnabled( "utils.potions.enabled", true ) ) {

				PrisonUtilsPotions utils = new PrisonUtilsPotions();

				utils.setEnablePotionEffects( isEnabled( "utils.potions.potionEffects", true ) );
				// utils.setEnablePotions( isEnabled( "utils.potions.potions.enabled", true ) );

				Prison.get().getCommandHandler().registerCommands( utils );

				modulesEnabled++;
			}
			
			
			if ( isEnabled( "utils.decay.enabled", true ) ) {

				PrisonUtilsDecay utils = new PrisonUtilsDecay();

				utils.setEnableDecayObby( isEnabled( "utils.decay.obby", true ) );
				utils.setEnableDecayRainbow( isEnabled( "utils.decay.rainbow", true ) );
				utils.setEnableDecayBlocks( isEnabled( "utils.decay.blocks", true ) );

				Prison.get().getCommandHandler().registerCommands( utils );

				modulesEnabled++;
			}
			
			if ( isEnabled( "utils.sounds.enabled", true ) ) {
				
				PrisonUtilsSounds utils = new PrisonUtilsSounds();
				
				utils.setEnableSoundEffects( isEnabled( "utils.sounds.playSound", true ) );
				
				Prison.get().getCommandHandler().registerCommands( utils );
				
				modulesEnabled++;
			}
			
			if ( isEnabled( "utils.titles.enabled", true ) ) {
				
				PrisonUtilsTitles utils = new PrisonUtilsTitles();
				
				utils.setEnableTitlesTitle( isEnabled( "utils.titles.title", true ) );
//				utils.setEnableTitlesSubtitle( isEnabled( "utils.titles.subtitle", true ) );
				utils.setEnableTitlesActionBar( isEnabled( "utils.titles.actionBar", true ) );
//				utils.setEnableTitlesClear( isEnabled( "utils.titles.clear", true ) );
//				utils.setEnableTitlesReset( isEnabled( "utils.titles.reset", true ) );
//				utils.setEnableTitlesTimes( isEnabled( "utils.titles.times", true ) );
				
				Prison.get().getCommandHandler().registerCommands( utils );
				
				modulesEnabled++;
			}
			
			if ( isEnabled( "utils.bombs.enabled", true ) ) {
				
				PrisonUtilsMineBombs utils = new PrisonUtilsMineBombs();
				
				utils.setEnableMineBombs( isEnabled( "utils.bombs.bombs", true ) );
				
				Prison.get().getCommandHandler().registerCommands( utils );
				
				// Only Register the Bomb Listener if bombs are active:
				Bukkit.getPluginManager().registerEvents(
								new PrisonBombListener( utils ), SpigotPrison.getInstance());
				
				modulesEnabled++;
			}
			
		}
		
		setModulesEnabled( modulesEnabled );
	}

	@Override
	public void deferredStartup() {
		
	}

	@Override
	public void disable() {
		
	}


	public YamlConfiguration getModulesConf() {
		return modulesConf;
	}
	
	private boolean isEnabled( String configPath, boolean defaultValue ) {
		return getModulesConf().getBoolean( configPath, defaultValue );
	}
	
    /**
     * For modules that have elements, this will return the count.  If a module has no
     * elements, then it will return a -1.  Otherwise a zero would indicate that a module
     * should have elements, but it currently has none.
     * 
     * Example would be ranks and mines.  For these, if it returns a zero, then they have 
     * no ranks or mines defined.  If it return a -1 then the module is not active.
     * 
     * @return
     */
    public int getElementCount() {
    	int results = isEnabled() ? 0 : -1;
    	
    	if ( isEnabled() ) {
    		results = getModulesEnabled();
    	}
    	
    	return results;
    }


	public int getModulesEnabled() {
		return modulesEnabled;
	}
	public void setModulesEnabled(int modulesEnabled) {
		this.modulesEnabled = modulesEnabled;
	}


}
