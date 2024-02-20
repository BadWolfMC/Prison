/*
 *  Prison is a Minecraft plugin for the prison game mode.
 *  Copyright (C) 2017-2020 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.commands;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.chat.FancyMessage;
import tech.mcprison.prison.commands.handlers.BlockArgumentHandler;
import tech.mcprison.prison.commands.handlers.DoubleArgumentHandler;
import tech.mcprison.prison.commands.handlers.DoubleClassArgumentHandler;
import tech.mcprison.prison.commands.handlers.IntegerArgumentHandler;
import tech.mcprison.prison.commands.handlers.IntegerClassArgumentandler;
import tech.mcprison.prison.commands.handlers.LongArgumentHandler;
import tech.mcprison.prison.commands.handlers.LongClassArgumentHandler;
import tech.mcprison.prison.commands.handlers.PlayerArgumentHandler;
import tech.mcprison.prison.commands.handlers.StringArgumentHandler;
import tech.mcprison.prison.commands.handlers.WorldArgumentHandler;
import tech.mcprison.prison.internal.CommandSender;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.internal.World;
import tech.mcprison.prison.internal.block.PrisonBlock;
import tech.mcprison.prison.output.ChatDisplay;
import tech.mcprison.prison.output.LogLevel;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.output.RowComponent;
import tech.mcprison.prison.util.ChatColor;

public class CommandHandler {

//	public static final String COMMAND_PRIMARY_ROOT_COMMAND = "prison";
//	public static final String COMMAND_FALLBACK_PREFIX = "prison";
	public static final String COMMAND_HELP_TEXT = "help";

	
	private Map<String, Object> registeredCommands;

	private TreeSet<RegisteredCommand> allRegisteredCommands;
	
    private Prison plugin;
    private Map<Class<?>, ArgumentHandler<?>> argumentHandlers =
    					new HashMap<Class<?>, ArgumentHandler<?>>();
    
    private Map<PluginCommand, RootCommand> rootCommands = new HashMap<>();


//	private List<PluginCommand> commands = new ArrayList<>();
	
	
	private TabCompleaterData tabCompleaterData;
	
//  private String helpSuffix = "help";

  public CommandHandler() {
      this.plugin = Prison.get();

      this.registeredCommands = new TreeMap<>();
      this.allRegisteredCommands = new TreeSet<>();
      
      this.tabCompleaterData = new TabCompleaterData();
        
        
      registerArgumentHandler(int.class, new IntegerArgumentHandler());
      registerArgumentHandler(double.class, new DoubleArgumentHandler());
      registerArgumentHandler(long.class, new LongArgumentHandler());
      
      registerArgumentHandler(Integer.class, new IntegerClassArgumentandler());
      registerArgumentHandler(Double.class, new DoubleClassArgumentHandler());
      registerArgumentHandler(Long.class, new LongClassArgumentHandler());

      registerArgumentHandler(String.class, new StringArgumentHandler());
      registerArgumentHandler(Player.class, new PlayerArgumentHandler());
      registerArgumentHandler(World.class, new WorldArgumentHandler());
      registerArgumentHandler(PrisonBlock.class, new BlockArgumentHandler());
      
      
      Output.get().logInfo( "&3Root command: &7/%s   &3fallback-prefix: &7%s",
      		DefaultSettings.COMMAND_PRIMARY_ROOT_COMMAND, DefaultSettings.COMMAND_FALLBACK_PREFIX );
  }

	   

      
    private PermissionHandler permissionHandler = (sender, permissions) -> {
        for (String perm : permissions) {
            if (!sender.hasPermission(perm)) {
                return false;
            }
        }
        return true;
    };

    private HelpHandler helpHandler = new HelpHandler() {
        private String formatArgument(CommandArgument argument) {
            String def = argument.getDefault();
            if (def.equals(" ")) {
                def = "";
            } else if (def.startsWith("?")) {
                String varName = def.substring(1);
                def = argument.getHandler().getVariableUserFriendlyName(varName);
                if (def == null) {
                    throw new IllegalArgumentException(
                        "The ArgumentVariable '" + varName + "' is not registered.");
                }
                def = ChatColor.GOLD + " | " + ChatColor.WHITE + def;
            } else {
                def = ChatColor.GOLD + " | " + ChatColor.WHITE + def;
            }

            return ChatColor.AQUA + "[" + argument.getName() + def + ChatColor.AQUA + "] "
                + ChatColor.DARK_AQUA + argument.getDescription();
        }

        @Override 
        public ChatDisplay getHelpMessage(CommandSender sender, RegisteredCommand command) {
        	
        	if ( !hasCommandAccess(sender, command, command.getLabel(), new String[0] ) ) {
        		return null;
        	}
        	
            ChatDisplay chatDisplay = new ChatDisplay(
            		String.format( "Cmd: &7%s", 
            					getUsageNoParameters(command)) );

            if (command.isSet() && command.getDescription() != null && !command.getDescription().isEmpty()) {
            	chatDisplay.addText(ChatColor.DARK_AQUA + command.getDescription());
            }

            chatDisplay.addText(getUsage(command));

            if (command.isSet()) {
                for (CommandArgument argument : command.getArguments()) {
                	chatDisplay.addText(formatArgument(argument));
                }
                if (command.getWildcard() != null) {
                	chatDisplay.addText(formatArgument(command.getWildcard()));
                }
                List<Flag> flags = command.getFlags();
                if (flags.size() > 0) {
                	chatDisplay.addText(ChatColor.DARK_AQUA + "Flags:");
                    for (Flag flag : flags) {
                        StringBuilder args = new StringBuilder();
                        for (FlagArgument argument : flag.getArguments()) {
                            args.append(" [" + argument.getName() + "]");
                        }
                        chatDisplay.addText("-" + flag.getIdentifier() + ChatColor.AQUA + args.toString());
                        for (FlagArgument argument : flag.getArguments()) {
                        	chatDisplay.addText(formatArgument(argument));
                        }
                    }
                }
                if ( command.getPermissions() != null && command.getPermissions().length > 0 ||
                	 command.getAltPermissions() != null && command.getAltPermissions().length > 0 ) {
                	
                	StringBuilder sb = new StringBuilder();
                	
                	if ( command.getPermissions() != null && command.getPermissions().length > 0 ) {
                		for ( String perm : command.getPermissions() ) {
                			if ( sb.length() > 0 ) {
                				sb.append( " " );
                			}
                			sb.append( perm );
                		}
                	}
            		if ( command.getAltPermissions() != null && command.getAltPermissions().length > 0 ) {
            			for ( String altPerm : command.getAltPermissions() ) {
            				if ( sb.length() > 0 ) {
            					sb.append( " " );
            				}
            				sb.append( altPerm );
            			}
            		}
            		
            		if ( sb.length() > 0 ) {
            			chatDisplay.addText(ChatColor.DARK_AQUA + "Permissions:");
            			
            			sb.insert( 0, ChatColor.AQUA );
            			sb.insert( 0, "   " );
            			chatDisplay.addText( sb.toString() );
            		}
                	
                }
                if ( command.getAliases() != null && command.getAliases().length > 0 ) {
                	
                	StringBuilder sb = new StringBuilder();
                	
                	if ( command.getAliases() != null && command.getAliases().length > 0 ) {
                		for ( String perm : command.getAliases() ) {
                			if ( sb.length() > 0 ) {
                				sb.append( " " );
                			}
                			sb.append( ChatColor.DARK_BLUE ).append( "[" )
                				.append( ChatColor.AQUA ).append( perm )
                				.append( ChatColor.DARK_BLUE ).append( "]" );
                		}
                	}
                	
                	if ( sb.length() > 0 ) {
                		chatDisplay.addText(ChatColor.DARK_AQUA + "Aliases:");
                		
                		sb.insert( 0, "   " );
                		chatDisplay.addText( sb.toString() );
                	}
                	
                }
                if ( command.getDocURLs() != null && command.getDocURLs().length > 0 ) {
                	
                	chatDisplay.addText(ChatColor.DARK_AQUA + "Documentation:");

                	for ( String docURL : command.getDocURLs() ) {
                		RowComponent row = new RowComponent();
                		
                		row.addTextComponent( "    " );
                		FancyMessage fMessage = new FancyMessage( docURL ).link( docURL )
                				.tooltip( "Click to open link" );
                		row.addFancy( fMessage );
                		
                		chatDisplay.addComponent( row );
                		
                		
                		
//                		StringBuilder sb = new StringBuilder();
//                		
//                		sb.append( "    " ).append( ChatColor.DARK_BLUE ).append( "[" )
//                					.append( ChatColor.AQUA ).append( docURL )
//                					.append( ChatColor.DARK_BLUE ).append( "]" );
//
//                		chatDisplay.addText( sb.toString() );
                	}
                	
                }
            }

            List<RegisteredCommand> subcommands = command.getSuffixes();
            if (subcommands.size() > 0) {
            	
                // Force a sorting by use of a TreeSet. Collections.sort() would not work.
                TreeSet<String> subCommandSet = new TreeSet<>();
                for (RegisteredCommand scommand : subcommands) {
                	
                	String sLabel = scommand.getCompleteLabel();
                	
                	if ( hasCommandAccess(sender, scommand, sLabel, new String[0] ) ) {
                		
                		String subCmd = scommand.getUsage();
                		
                		int subCmdSubCnt = scommand.getSuffixes().size();
                		String subCommands = (subCmdSubCnt == 0 ? "" : 
                			ChatColor.DARK_AQUA + "(" + subCmdSubCnt + " Subcommands)");
                		
                		String isAlias = scommand.isAlias() ? ChatColor.DARK_AQUA + "  Alias" : "";
                		
                		subCommandSet.add(  
                				String.format( "%s %s %s", subCmd, subCommands, isAlias ));
                	}
                	
                }

                // Only if there are entries to show, then include the header and the details
                if ( subCommandSet.size() > 0 ) {
                	chatDisplay.addText(ChatColor.DARK_AQUA + "Subcommands:");
                	
                	for (String subCmd : subCommandSet) {
                		chatDisplay.addText(subCmd);
                	}
                }
            }
            
            if ( command.getLabel().equalsIgnoreCase( DefaultSettings.COMMAND_PRIMARY_ROOT_COMMAND ) && 
            									rootCommands.size() > 1 ) {
            	
            	ArrayList<String> rootCommandsMessages = buildHelpRootCommands();
            	if ( rootCommandsMessages.size() > 1 ) {
            		for ( String rootCmd : rootCommandsMessages )
					{
            			chatDisplay.addText( rootCmd );
					}
            		
            	}

            	ArrayList<String> aliasesMessages = buildHelpAliases();
            	if ( aliasesMessages.size() > 1 ) {
            		for ( String alias : aliasesMessages )
					{
            			chatDisplay.addText( alias );
					}
            		
            	}
            	
            	ArrayList<String> excludedWorlds = buildExcludedWorlds();
            	if ( excludedWorlds.size() > 1 ) {
            		for ( String excludedWorld : excludedWorlds )
            		{
            			chatDisplay.addText( excludedWorld );
            		}
            		
            	}
            	
            	
            }
            

            return chatDisplay;
//            return message.toArray(new String[0]);
        }

        private ArrayList<String> buildExcludedWorlds() {
        	
			ArrayList<String> message = new ArrayList<>();
			
			message.add(ChatColor.DARK_AQUA + "Prison is disabled in the following Worlds:");
			
			TreeSet<String> excludedWorlds = Prison.get().getPlatform().getExcludedWorlds();
			
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for ( String world : excludedWorlds )
			{
				if ( sb.length() > 0 ) {
					sb.append( ", " );
				}
				
				if ( count++ > 5 ) {
				
					message.add( "  " + sb.toString() );
					sb.setLength( 0 );
					count = 0;
				}

				sb.append( world );
				
			}
			
			if ( sb.length() > 0 ) {
				message.add( "  " + sb.toString() );
				
			}

            return message;
        }
        
		private ArrayList<String> buildHelpRootCommands() {
			ArrayList<String> message = new ArrayList<>();
			
			message.add(ChatColor.DARK_AQUA + "Root Commands:");
                // Force a sorting by use of a TreeSet. Collections.sort() would not work.
                TreeSet<String> rootCommandSet = new TreeSet<>();

            	// Try adding in all other root commands:
			Set<PluginCommand> rootKeys = getRootCommands().keySet();
            	
            	for ( PluginCommand rootKey : rootKeys ) {
				StringBuilder sbAliases = new StringBuilder();
				
				// Do not list aliases:
				if ( !(rootKey.getRegisteredCommand().isAlias() && rootKey.getRegisteredCommand().getParentOfAlias() != null) ) {
//					String isAlias = rootKey.getRegisteredCommand().isAlias() ? ChatColor.DARK_AQUA + "  Alias" : "";
            		
//					if ( rootKey.getRegisteredCommand().getRegisteredAliases().size() > 0 ) {
//						for ( RegisteredCommand alias : rootKey.getRegisteredCommand().getRegisteredAliases() ) {
//							
//							sbAliases.append( ChatColor.DARK_BLUE ).append( "[" ).append( ChatColor.AQUA ).append( "/" )
//							.append( getRootCommandRegisteredLabel(alias) )
//							.append( ChatColor.DARK_BLUE ).append( "] " );
//						}
//						sbAliases.insert( 0, 
//								new StringBuilder().append( ChatColor.DARK_AQUA ).
//								append( "Aliases: " ).append( ChatColor.AQUA ));
//					}
					String rootCmd = 
							String.format( "%s  %s", 
									rootKey.getUsage(), sbAliases.toString() );
					
            		rootCommandSet.add( rootCmd );
				}

            		
            	}
            	
            	for (String rootCmd : rootCommandSet) {
            		message.add(rootCmd);
            	}
			
			return message;
            }
            
		/**
		 * This builds a list of all  the aliases that exist.
		 * 
		 * @return
		 */
		private ArrayList<String> buildHelpAliases() {
			ArrayList<String> message = new ArrayList<>();
			
			message.add(ChatColor.DARK_AQUA + "Aliases:");

			// Force a sorting by use of a TreeSet. Collections.sort() would not work.
			TreeSet<String> aliasesSet = new TreeSet<>();
			
			
			for ( RegisteredCommand regCmd : getAllRegisteredCommands() ) {
				buildHelpAliasMessage( regCmd, aliasesSet );
//				plugin.logDebug( "### CommandHandler.buildHelpAliases ### test: %s ", regCmd.toString() );
			}
			
			
//			// Try adding in all other root commands:
//			Set<PluginCommand> rootKeys = getRootCommands().keySet();
//			for ( PluginCommand rootKey : rootKeys ) {
//				
//				plugin.logDebug( "### CommandHandler.buildHelpAliases ### rootCommands: %s ", rootKey.toString() );
//				RegisteredCommand registeredCommand = rootKey.getRegisteredCommand();
//				
//				buildHelpAliases( registeredCommand,  aliasesSet );
//			}
			
			// Sorted results, add to the List:
			for (String rootCmd : aliasesSet) {
				message.add(rootCmd);
			}
			
			return message;
		}
		
//		private void buildHelpAliases( RegisteredCommand registeredCommand, TreeSet<String> aliasesSet ) {
//			buildHelpAliasMessage( registeredCommand, aliasesSet );
//			
//			plugin.logDebug( "### CommandHandler.buildHelpAliases ### : %s ", registeredCommand.toString() );
//
//			for ( RegisteredCommand suffixRegCmd : registeredCommand.getSuffixes() ) {
//				
//				buildHelpAliases( suffixRegCmd,  aliasesSet );
//			}
//			
//		}

		private void buildHelpAliasMessage( RegisteredCommand registeredCommand, TreeSet<String> aliasesSet ) {
			if ( registeredCommand.isAlias() && registeredCommand.getParentOfAlias() != null) {

				StringBuilder sbAliases = new StringBuilder();
				
				sbAliases.append( ChatColor.DARK_BLUE ).append( "(" ).append( ChatColor.AQUA )
				.append( registeredCommand.getParentOfAlias().getUsage() )
				.append( ChatColor.DARK_BLUE ).append( ")" );

				String rootCmd = 
						String.format( "%s  %s", 
								registeredCommand.getUsage(),
								sbAliases.toString() );

				aliasesSet.add( rootCmd );
			}
        }

        /**
         * <p>If the registration of this command was not successful as the original 
         * label, it would have had the fallback prefix added until the command
         * was unique. Therefore, use that registered label instead so the users
         * will know what command they need to enter.
         * </p>
         * 
         * @param label
         * @param command
         * @return 
         */
        private String getRootCommandRegisteredLabel(RegisteredCommand command ) {
        	String commandLabel = command.getLabel();
        	if ( command instanceof RootCommand ) {
        		RootCommand rootCommand = (RootCommand) command;
        		if ( rootCommand.getBukkitCommand().getLabelRegistered() != null ) {
        			commandLabel = rootCommand.getBukkitCommand().getLabelRegistered();
        		}
        	}
        	return commandLabel;
        }
        
        @Override
        public String getUsageNoParameters(RegisteredCommand command) {
            StringBuilder usage = new StringBuilder();

            String cmdLabel = getRootCommandRegisteredLabel( command );
            usage.append(cmdLabel);

            RegisteredCommand parent = command.getParent();
            while (parent != null) {
            	String label = getRootCommandRegisteredLabel( parent );
                usage.insert(0, label + " ");
                parent = parent.getParent();
            }

            usage.insert(0, "/");
            
            return usage.toString();
        }
        
        @Override 
        public String getUsage(RegisteredCommand command) {
            StringBuilder usage = new StringBuilder();

            String cmdLabel = getRootCommandRegisteredLabel( command );
            usage.append(cmdLabel);

            RegisteredCommand parent = command.getParent();
            while (parent != null) {
            	String label = getRootCommandRegisteredLabel( parent );
                usage.insert(0, label + " ");
                parent = parent.getParent();
            }

            usage.insert(0, "/");

            if (!command.isSet()) {
                return usage.toString();
            }

            usage.append(ChatColor.AQUA);

            for (CommandArgument argument : command.getArguments()) {
                usage.append(" [" + argument.getName() + "]");
            }

            usage.append(ChatColor.WHITE);

            for (Flag flag : command.getFlags()) {
                usage.append(" (-" + flag.getIdentifier() + ChatColor.AQUA);
                for (FlagArgument arg : flag.getArguments()) {
                    usage.append(" [" + arg.getName() + "]");
                }
                usage.append(ChatColor.WHITE + ")");
            }

            if (command.getWildcard() != null) {
                usage.append(ChatColor.AQUA + " [" + command.getWildcard().getName() + "]");
            }

            return usage.toString();
        }
    };


    @SuppressWarnings("unchecked")
    public <T> ArgumentHandler<? extends T> getArgumentHandler(Class<T> clazz) {
        return (ArgumentHandler<? extends T>) argumentHandlers.get(clazz);
    }

    public HelpHandler getHelpHandler() {
        return helpHandler;
    }

    public void setHelpHandler(HelpHandler helpHandler) {
        this.helpHandler = helpHandler;
    }

    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    public void setPermissionHandler(PermissionHandler permissionHandler) {
        this.permissionHandler = permissionHandler;
    }

    public <T> void registerArgumentHandler(Class<? extends T> clazz,
        ArgumentHandler<T> argHandler) {
        if (argumentHandlers.get(clazz) != null) {
            throw new IllegalArgumentException(
                "There is already a ArgumentHandler bound to the class " + clazz.getName() + ".");
        }

        argHandler.handler = this;
        argumentHandlers.put(clazz, argHandler);
    }
    
    public Object getRegisteredCommandClass( @SuppressWarnings( "rawtypes" ) Class commandClass ) {
    	Object results = null;
    	
    	String key = commandClass.getSimpleName();
    	if ( key != null && getRegisteredCommands().containsKey( key ) ) {
    		results = getRegisteredCommands().get( key );
    	}
    	
    	return results;
    }

    public void registerCommands(Object methodInstance) {

    	// Keep a reference to the registered command object so it can be 
    	// accessed in the future if needed for other uses.
    	getRegisteredCommands().put( methodInstance.getClass().getSimpleName(), methodInstance );
    	
    	for (Method method : methodInstance.getClass().getDeclaredMethods()) {
            Command commandAnno = method.getAnnotation(Command.class);
            if (commandAnno == null) {
                continue;
            }

            RegisteredCommand mainCommand = commandRegisterConfig( method, commandAnno, methodInstance );
            
            
            String[] aliases = addConfigAliases( commandAnno.identifier(), commandAnno.aliases() );

            if ( aliases.length > 0 ) {
//            	if ( commandAnno.aliases() != null && commandAnno.aliases().length > 0 ) {
            	
            	
            	for ( String alias : aliases )
//            		for ( String alias : commandAnno.aliases() )
				{
					RegisteredCommand aliasCommand = commandRegisterConfig( method, commandAnno, methodInstance, alias );
            		
					// Add the alias to the primary RegisteredCommand to track it's own aliases:
            		mainCommand.getRegisteredAliases().add( aliasCommand );
            		aliasCommand.setParentOfAlias( mainCommand );
				}
            	
            }
           
        }
    }

    private RegisteredCommand commandRegisterConfig( Method method, Command commandAnno, Object methodInstance ) {
    	return commandRegisterConfig( method, commandAnno, methodInstance, null );
    }
    
	private RegisteredCommand commandRegisterConfig( Method method, Command commandAnno, 
						Object methodInstance, String alias ) {
        String[] identifiers = ( alias == null ? commandAnno.identifier() : alias).split(" ");
        
            if (identifiers.length == 0) {
                throw new RegisterCommandMethodException(method, "Invalid identifiers");
            }

        String label = identifiers[0];

        PluginCommand rootPluginCommand = plugin.getPlatform().getCommand(label).orElse( null );

        if ( rootPluginCommand == null ) {
        	
        	String[] aliases = addConfigAliases( commandAnno.identifier(), commandAnno.aliases() );
        	rootPluginCommand = new PluginCommand(label, 
        						commandAnno.description(),
        						"/" + label,
        						aliases );
//        	rootPluginCommand = new PluginCommand(label, 
//			        			commandAnno.description(),
//			        			"/" + label,
//			        			commandAnno.aliases() );
        	plugin.getPlatform().registerCommand(rootPluginCommand);
        }


        // If getRootCommands() does not contain the rootPCommand then add it:
        if ( !getRootCommands().containsKey( rootPluginCommand ) ) {
        	RootCommand rootRegisteredCommand = new RootCommand( rootPluginCommand, this );
        	rootRegisteredCommand.setAlias( alias != null );
        	
        	// Must add all new RegisteredCommand objects to both getAllRegisteredCommands() and
        	// getTabCompleterData().
        	getAllRegisteredCommands().add( rootRegisteredCommand );
        	getTabCompleaterData().add( rootRegisteredCommand );
        	
        	getRootCommands().put( rootPluginCommand, rootRegisteredCommand );
        }
        
        RegisteredCommand mainCommand = getRootCommands().get( rootPluginCommand );
        
        for (int i = 1; i < identifiers.length; i++) {
        	
            String suffix = identifiers[i];
            if ( mainCommand.doesSuffixCommandExist(suffix) ) {
                mainCommand = mainCommand.getSuffixCommand(suffix);
            } 
            else {
                RegisteredCommand newCommand = new RegisteredCommand(suffix, this, mainCommand );
                newCommand.setAlias( alias != null );
                mainCommand.addSuffixCommand(suffix, newCommand);
            
                // Must add all new RegisteredCommand objects to both getAllRegisteredCommands() and
                // getTabCompleterData().
                getAllRegisteredCommands().add( newCommand );
                getTabCompleaterData().add( newCommand );

                mainCommand = newCommand;
            }
        }

        // Associate the last RegisteredCommand (mainCommand) with the rootPCommand since that is
        // the leaf-node that will be tied to the registered command, especially with aliases:
        rootPluginCommand.setRegisteredCommand( mainCommand );
        

            // Validate that the first parameter, if it exists, is actually a CommandSender:
            if ( method.getParameterCount() > 0 ) {
            	
        	// The first parameter "should" always be CommandSender or there will be difficult
        	// to trace failures at runtime:
            	Class<?> cmdSender = method.getParameterTypes()[0];
            	
            	if ( !cmdSender.getSimpleName().equalsIgnoreCase( "CommandSender") ) {
            		Output.get().logWarn( 
            			String.format( 
	            			"Possible issue has been detected with " +
	            			"registering a command where " +
            				"the first parameter is not a CommandSender: " +
            				"class = [%s] method = [%s] first parameter type = [%s]",
            				method.getDeclaringClass().getSimpleName(), method.getName(),
            				cmdSender.getSimpleName()
            					));
            	}
            	
            }
            
            mainCommand.set(methodInstance, method);
        return mainCommand;
	}


    public static String[] addConfigAliases( String label, String[] aliases )
	{
    	String[] results = aliases;
    	
    	String configKey = "prisonCommandHandler.aliases." + label.replace( " ", "." );
    	
    	List<?> ca = Prison.get().getPlatform().getConfigStringArray( configKey );
    	if ( ca != null && ca.size() > 0 && ca.get( 0 ) instanceof String ) {
    		
			List<String> configAliases = new ArrayList<>();
			
			for ( String alias : aliases ) {
				configAliases.add( alias );
			}
					
			for ( Object aliasObj : ca ) {
				if ( aliasObj instanceof String ) {
					configAliases.add( aliasObj.toString() );
				}
			}
			
    		results = configAliases.toArray( new String[0] );
    		
    	}
		return results;
	}
    
    /***
     * <p>
     * This function is strictly for non-ops, and if a player has a given perm that
     * is specified in the config file, then it will lock that player out of that
     * command.  This is intended to force overrides on commands for the commands 
     * that do not have their own perms.
     * </p>
     * @return
     */
    public boolean hasCommandAccess( CommandSender sender, RegisteredCommand rootCommand, 
    		String label, String[] args ) {

    	CommandAccessResults results = new CommandAccessResults( sender );
    	
    	hasCommandAccess( sender, rootCommand, label, args, results );
    	
    	if ( results.isAccess() ) {
    		results.setAccessPermitted();
    	}
    	
    	if ( !results.isAccess() ) {
    		// Debug logging if prison is in debug mode:
    		results.debugAccess();
    	}
    	
    	return results.isAccess();
    }
    
    private void hasCommandAccess( CommandSender sender, RegisteredCommand rootCommand, 
    			String label, String[] args,
    			CommandAccessResults results ) {
    		
//    	boolean results = true;
    	
    	if ( !sender.isOp() ) {
    		
    		boolean hasAccess = rootCommand.testPermission(sender);
    		
    		if ( !hasAccess ) {
    			results.setAccess( false );
    		}
    		else {
    			
    			String exRAKey = "prisonCommandHandler.exclude-non-ops.exclude-related-aliases";
    			boolean excludeRelatedAliases = getConfigBoolean( exRAKey );
    			
    			String sLabelAlias = !excludeRelatedAliases || rootCommand.getParentOfAlias() == null ? 
    					null : rootCommand.getParentOfAlias().getCompleteLabel();
    			
    			
    			commandAccessPermChecks( sender, rootCommand, label, results );
    			
    			if ( results.isAccess() && sLabelAlias != null ) {
    				
    				commandAccessPermChecks( sender, rootCommand.getParentOfAlias(), sLabelAlias, results );
    			}
    		}
    		

    	}
    	
    	// If we get to this point, and the result is true (the player has access the
    	// specified command so far), and there are more args, we need to next
    	// take the args[0] and append it to the label, and then test it again.
    	// This needs to continue until the generated command is rejected, or
    	// it passes it's clean and the player has full access to the command(s).
    	if ( results.isAccess() && args.length > 0 ) {
    		String newSuffix = args[0];
    		String newLabel = label + " " + newSuffix;
    		String[] newArgs = Arrays.copyOfRange( args, 1, args.length );
    		
    		RegisteredCommand newSuffixCommand = rootCommand.getSuffixCommand( newSuffix );
    		
    		if ( newSuffixCommand != null ) {
    			
    			hasCommandAccess( sender, newSuffixCommand, 
    					newLabel, newArgs, results );
    		}
    	}
    	
    }

	private void commandAccessPermChecks(CommandSender sender, RegisteredCommand rootCommand, String label,
			CommandAccessResults results) {
		
		// Must first check to see if the command is setup for excludes, and if 
		// not then exit with a value of true:
		String configKey = "prisonCommandHandler.exclude-non-ops.commands." + 
					label.replace( " ", "." );
		
		List<?> excludePerms = getConfigStringArray( configKey + ".perms" );
		boolean includeCommandPerms = getConfigBoolean( configKey + ".includeCmdPerms" );
		boolean includeCommandAltPerms = getConfigBoolean( configKey + ".includeCmdAltPerms" );
		
		if ( excludePerms != null && excludePerms.size() > 0 ) {

			// first check the command perms first:
			if ( includeCommandPerms ) {
				
				for ( String perm : rootCommand.getPermissions() ) {
					
					if ( sender.hasPermission( perm ) ) {
						results.rejectCommandPermission( label, perm );
						break;
					}
				}
			}
			
			if ( results.isAccess() && includeCommandAltPerms ) {
				
				// first check the command altPerms next:
				for ( String altPerm : rootCommand.getAltPermissions() ) {
					
					if ( sender.hasPermission( altPerm ) ) {
						results.rejectCommandAltPermission( label, altPerm );
						break;
					}
				}
			}
			
			// If results has not been set to false, then check the exclude-non-ops:
			if ( results.isAccess() ) {
				
				if ( excludePerms != null && excludePerms.size() > 0 && excludePerms.get( 0 ) instanceof String ) {
					
					for ( Object permObj : excludePerms) {
						if ( permObj instanceof String ) {
							if ( sender.hasPermission( permObj.toString() ) ) {
								results.rejectConfigYaml( label, permObj.toString() );
								break;
							}
						}
					}
				}
				
			}
		}
	}

    private boolean getConfigBoolean( String configKey ) {
    	return Prison.get().getPlatform().getConfigBooleanFalse( configKey );
    }
    private List<?> getConfigStringArray( String configKey ) {
    	return Prison.get().getPlatform().getConfigStringArray( configKey );
    }
    
	public boolean onCommand(CommandSender sender, PluginCommand command, String label,
    								String[] args) {
    	
        RootCommand rootCommand = rootCommands.get(command);
        if (rootCommand == null) {
        	Output.get().logError( "CommandHandler.onCommand(): " + command.getLabel() + 
        			" : No root command found. " );
            return false;
        }
        
        if (rootCommand.isOnlyPlayers() && !(sender instanceof Player)) {
            Prison.get().getLocaleManager().getLocalizable("cantAsConsole")
                .sendTo(sender, LogLevel.ERROR);
            return true;
        }
        
        else if ( !hasCommandAccess( sender, rootCommand, label, args ) ) {
        	// The player does not have access to this command.
        	// Who cares!  Just exit and do nothing. Never log this.
        	return true;
        }
        
        else {

        	try {
        		rootCommand.execute(sender, args);
        	}
        	catch ( Exception e ) {
        		String message = "Prison CommandHander: onCommand: " + e.getMessage() + 
        				" [" + e.getCause() == null ? "cause not reported" : e.getCause() + "]"; 
        		
        		Output.get().logError( message );
        		for ( StackTraceElement ste : e.getStackTrace() ) {
        			Output.get().logError( ste.toString() );
				}
        		
        	}
        }
        

        return true;
    }
	
	/**
	 * <p>This function is similar to onCommand, but it does not run any commands.
	 * It only validates if a player has access to the commands.
	 * </p>
	 * @param registeredCommand 
	 * 
	 * @return
	 */
	public boolean checkCommand( CommandSender sender, 
					RegisteredCommand registeredCommand, 
					String label, String... args ) {
		boolean hasAccess = true;
				
		if ( sender != null &&
				registeredCommand != null &&
				!hasCommandAccess( sender, registeredCommand, label, args ) ) {
			
			// The player does not have access to this command.
			// Who cares!  Just exit and do nothing. Never log this.
			hasAccess = false;
		}
		
		
		return hasAccess;
	}
    
	public Map<String, Object> getRegisteredCommands() {
		return registeredCommands;
	}
	public void setRegisteredCommands( Map<String, Object> registeredCommands ) {
		this.registeredCommands = registeredCommands;
	}
	
	public TreeSet<RegisteredCommand> getAllRegisteredCommands() {
		return allRegisteredCommands;
	}

	public Map<PluginCommand, RootCommand> getRootCommands() {
		return rootCommands;
	}
	public void setRootCommands( Map<PluginCommand, RootCommand> rootCommands ) {
		this.rootCommands = rootCommands;
	}

//	private List<PluginCommand> getCommands() {
//		return commands;
//	}

    public TabCompleaterData getTabCompleaterData() {
		return tabCompleaterData;
	}

/*
 * ###Tab-Complete###
 * 
 * Disabled for now until a full solution can be implemented for tab complete.
 * 
    public List<String> getRootCommandKeys() {
    	List<String> results = new ArrayList<>();
    	
    	Set<PluginCommand> keys = rootCommands.keySet();
    	for ( PluginCommand pluginCommand : keys ) {
    		// These are the core command sets:
			results.add( pluginCommand.getLabel() );
			
			// Then expand them to all the sub commands that are assoicated with the cores:
			RootCommand cmd = rootCommands.get( pluginCommand );
			List<RegisteredCommand> regCmds = cmd.getSuffixes();
			for ( RegisteredCommand regCmd : regCmds ) {
				results.add( pluginCommand.getLabel() + " " + regCmd.getLabel() );
			}
			
		}
    	
    	return results;
    }
 */
   
    /**
     * <p>This takes a prison command as entered on the command line and will translate it to the
     * command that was actually registered.  If attempting to register a command, and bukkit
     * finds that another plugin already registered that command, it will prefix it with the plugin's
     * prefix until it is unique.  This function will ensure that we use the registered version
     * of the command that is specified.
     * </p>
     * 
     * @param command
     * @return
     */
    public String findRegisteredCommand(String command) {
    	
    	String[] patternParts = command.split( " " );
    	
    	if ( patternParts.length > 0 ) {
    		String rootPattern = patternParts[0];
    		
    		for ( RegisteredCommand cmd : allRegisteredCommands ) {
    			
    			if ( cmd.getLabel().equalsIgnoreCase( rootPattern ) ) {
    				
    				if ( cmd.isRoot() ) {
    					
    					RootCommand rootCommand = (RootCommand) cmd;
    					if ( rootCommand.getBukkitCommand().getLabelRegistered() != null ) {

    						patternParts[0] = rootCommand.getBukkitCommand().getLabelRegistered();
    					}
    				}
    			}
    		}
    	}
    	
    	return String.join( " ", patternParts );
    }
}
