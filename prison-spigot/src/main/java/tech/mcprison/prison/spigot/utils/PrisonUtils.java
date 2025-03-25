package tech.mcprison.prison.spigot.utils;

import org.bukkit.Bukkit;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.CommandSender;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.ranks.data.RankPlayer;
import tech.mcprison.prison.spigot.game.SpigotPlayer;
import tech.mcprison.prison.spigot.game.SpigotPlayerException;

public abstract class PrisonUtils
		implements PrisonUtilsInterface {
	
	private String pluginName;
	private boolean pluginRegistered = false;
	private String pluginVersion;
	
	
	private Boolean enabled = null;
	
	
	/**
	 * <p>This constructor is for classes that do not need to access other plugins.
	 * </p>
	 * 
	 */
	public PrisonUtils() {
		super();
	}
	
	/**
	 * <p>This constructor is for setting up and initializing access to other plugins.
	 * </p>
	 * 
	 * @param pluginName
	 */
	public PrisonUtils( String pluginName ) {
		super();
		
		this.pluginName = pluginName;
		
		pluginInitialize();
	}
	
	
	private void pluginInitialize() {
		
		setPluginRegistered( Bukkit.getPluginManager().isPluginEnabled( getPluginName() ) );
		
    	setPluginVersion( isPluginRegistered() ? 
    			Bukkit.getPluginManager().getPlugin( getPluginName() )
    											.getDescription().getVersion() : null );
		
    	this.enabled = initialize();
	}
	
	@Override
	public boolean isEnabled() {

		
		return enabled.booleanValue();
	}
	
	/**
	 * <p>This function performs all of the initialization and to determine if the 
	 * module should be, or could be, enabled.
	 * </p>
	 * 
	 * @return
	 */
	abstract protected Boolean initialize();

//	protected SpigotPlayer checkPlayerPerms( CommandSender sender, String playerName, 
//			String permsSelf, String permsOthers ) {
//		return checkPlayerPerms(sender, playerName, permsSelf, permsOthers, false );
//	}
	
	/**
	 * <p>This function checks the perms of the player.  If the CommandSender has no 
	 * permission to run these commands, then this function will return a null which is
	 * indicates the command cannot be ran.
	 * </p>
	 * 
	 * <p>Since playerName is optional, it may contain values from options so it is
	 * perfectly fine if playerName does not map to an actual player.  If playerName
	 * contains options, this function does not need to deal with that situation since
	 * it's handled within the options processing.
	 * </p>
	 * 
	 * @param sender
	 * @param playerName
	 * @param permsSelf
	 * @param permsOthers
	 * @return
	 */
	protected SpigotPlayer checkPlayerPerms( CommandSender sender, String playerName, 
					String permsSelf, String permsOthers ) {
		
		boolean isConsole = !(sender instanceof org.bukkit.entity.Player);
		
    	SpigotPlayer player = getSpigotPlayer( playerName );
    	
    	// Player's name was not found then it's either being ran from console, or on self.
    	if ( player == null ) {
    		
    		// Ran from console, so if player is null, then playerName was either not
    		// specified or was invalid:
    		if ( isConsole ) {
    			sender.sendMessage( String.format(
    					"&3PlayerName is incorrect or they are not online. [&7%s&3]", 
    					(playerName == null ? "null" : playerName) ));
    		}
    		else if ( !sender.isOp() && !sender.hasPermission( permsSelf ) ) {
    			sender.sendMessage( String.format(
    					"&3You do not have the permission to use on another player. [&7%s&3]", 
    					(playerName == null ? "null" : playerName) ));
    				
    		}
    		else {
    			
    			// sender is able to use the command on their self:
    			player = new SpigotPlayer( (org.bukkit.entity.Player) sender);
    		}

    	}
    	else {
    		
    		// The provided playerName was valid.
    		
    		// Need to confirm the sender is either console, OP, or has perms to 
    		// use command on someone else, if none of these apply, then reject
    		// the request to run the command.
    		if ( !isConsole && !sender.isOp() && !sender.hasPermission( permsOthers ) ) {
    			sender.sendMessage( String.format(
    					"&3You do not have the permission to use on another player. [%s]", 
    					(playerName == null ? "null" : playerName) ));

    			// Set player to null to indicate authentication failed:
    			player = null;
    		}
    	}
    	
    	return player;
	}
	
	
    /**
     * <p>Gets a player by name.  If the player is not online, then try to get them from 
     * the offline player list. If not one is found, then return a null.
     * </p>
     * 
     * <p>The getOfflinePlayer() will now include RankPlayer as a fall back to help
     * ensure a player is always returned, if its a valid player.
     * </p>
     * 
     * @param playerName 
     * @return Player if found, or null.
     */
	protected SpigotPlayer getSpigotPlayer( String playerName ) {
		SpigotPlayer result = null;
		
		RankPlayer rPlayer = Prison.get().getPlatform().getRankPlayer( null, playerName );
		
//		// First try to get the bukkit online player:
//		if ( rPlayer != null ) {
//			org.bukkit.entity.Player playerBukkit = Bukkit.getPlayer( rPlayer.getUUID() );
//		}
		
		if ( rPlayer == null ) {
		
			// The requested player name is not within prison. Could be a typo or they 
			// don't have ranks enabled.  First get a temp SpigotPlayer and then use 
			// the supplied name and UUID to create a new RankPlayer so we can 
			// create a valid SpigotPlayer that includes the RankPlayer even with the
			// Ranks disabled.
			Player player = Prison.get().getPlatform().getPlayer( playerName ).orElse( null );
			
			if ( player != null ) {
				result = (SpigotPlayer) player;
				
				// Create a 'false' RankPlayer object:
				
				RankPlayer rp = new RankPlayer( player.getUUID(), player.getDisplayName() );

				rPlayer = rp;
			}
			
			
		}
		
		
		if ( rPlayer != null ) {
			
			result = SpigotPlayer.getSpigotPlayer( rPlayer );
//			try {
//			} 
//			catch (SpigotPlayerException e) {
//				
////				String msg = String.format(
////						"SpigotPlayer: Could not get a bukkit player object for '%s'. "
////						+ "Is their name spelled correctly? "
////						+ "Have they been removed or banned from spigot?",
////						playerName
////						);
//				
//				Output.get().logInfo( e.getMessage() );
//				
//			}
			
//			Player player = Prison.get().getPlatform().getPlayer( rPlayer.getUUID() ).orElse(null);
//			
//			if ( player != null ) {
//				result = (SpigotPlayer) player;
//			}
//			else if ( useOfflinePlayer ) {
//				
//				
////				Player offLinePlayer = Prison.get().getPlatform().getOfflinePlayer( rPlayer.getUUID() ).orElse( null );
////				
////				if ( offLinePlayer != null ) {
////					
////					SpigotOfflinePlayer offlinePlayer = (SpigotOfflinePlayer) offLinePlayer;
////					
////					result = new SpigotPlayer( offlinePlayer.getWrapper().getPlayer() );
//				}
//			}
		}
		
//		if ( playerName != null ) {
//			Optional<Player> opt = Prison.get().getPlatform().getPlayer( playerName );
//			
//			if ( opt.isPresent() ) {
//				result = (SpigotPlayer) opt.get();
//			}
//			else if ( useOfflinePlayer ) {
//				Optional<Player> optOLP = Prison.get().getPlatform().getOfflinePlayer( playerName );
//				
//				if ( optOLP.isPresent() ) {
//					
//					SpigotOfflinePlayer offlinePlayer = (SpigotOfflinePlayer) optOLP.get();
//					
//					result = new SpigotPlayer( offlinePlayer.getWrapper().getPlayer() );
//				}
//			}
//			
//		}
		return result;
	}
	
	/**
	 * <p>This parses a String value to an int.  It uses a default value and
	 * also constrains the results to be between within a given range.
	 * </p>
	 * 
	 * @param value
	 * @param defaultValue
	 * @param rangeLow
	 * @param rangeHigh
	 * @return
	 */
	protected int intValue( String value, int defaultValue, int rangeLow, int rangeHigh ) {
		int results = defaultValue;
		
		if ( value != null && !value.trim().isEmpty() ) {
			try {
				results = Integer.parseInt( value );
			}
			catch ( NumberFormatException e ) {
				// Not a valid number so ignore
			}
			
			if ( results < rangeLow ) {
				results = rangeLow;
			}
			else if ( results > rangeHigh ) {
				results = rangeHigh;
			}
		}
		
		return results;
	}

	public int parseInt(String s){
		int number = 0;

		if ( s != null && !s.trim().isEmpty() ) {
			
			try{
				number = Integer.parseInt(s);
			}catch(NumberFormatException ex){
				ex.printStackTrace();
			}
		}

		return number;
	}
	
	/**
	 * <p>Parses a value to a float.  It uses the supplied default value and
	 * also constrains the results by the parameters.
	 * </p>
	 * 
	 * @param value
	 * @param defaultValue
	 * @param rangeLow
	 * @param rangeHigh
	 * @return
	 */
	protected float floatValue( String value, float defaultValue, float rangeLow, float rangeHigh ) {
		float results = defaultValue;
		
		if ( value != null && !value.trim().isEmpty() ) {
			try {
				results = Float.parseFloat( value );
			}
			catch ( NumberFormatException e ) {
				// Not a valid number so ignore
			}
			
			if ( results < rangeLow ) {
				results = rangeLow;
			}
			else if ( results > rangeHigh ) {
				results = rangeHigh;
			}
		}
		
		return results;
	}
	/**
	 * <p>Parses a value to a double.  It uses the supplied default value and
	 * also constrains the results by the parameters.
	 * </p>
	 * 
	 * @param value
	 * @param defaultValue
	 * @param rangeLow
	 * @param rangeHigh
	 * @return
	 */
	protected double doubleValue( String value, double defaultValue, double rangeLow, double rangeHigh ) {
		double results = defaultValue;
		
		if ( value != null && !value.trim().isEmpty() ) {
			try {
				results = Double.parseDouble( value );
			}
			catch ( NumberFormatException e ) {
				// Not a valid number so ignore
			}
			
			if ( results < rangeLow ) {
				results = rangeLow;
			}
			else if ( results > rangeHigh ) {
				results = rangeHigh;
			}
		}
		
		return results;
	}
	
	public String getPluginName() {
		return pluginName;
	}
	public void setPluginName( String pluginName ) {
		this.pluginName = pluginName;
	}

	public boolean isPluginRegistered() {
		return pluginRegistered;
	}
	public void setPluginRegistered( boolean pluginRegistered ) {
		this.pluginRegistered = pluginRegistered;
	}

	public String getPluginVersion() {
		return pluginVersion;
	}
	public void setPluginVersion( String pluginVersion ) {
		this.pluginVersion = pluginVersion;
	}
}
