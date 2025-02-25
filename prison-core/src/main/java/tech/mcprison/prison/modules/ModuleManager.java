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

package tech.mcprison.prison.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.PrisonAPI;
import tech.mcprison.prison.modules.ModuleStatus.Status;
import tech.mcprison.prison.output.Output;

/**
 * Keeps track of each module and each module's status.
 *
 * @author Faizaan A. Datoo
 * @since API 1.0
 */
public class ModuleManager {

	public static final String MODULE_MANAGER_DIRECTORY = "module_conf";
	
	public static final String MODULE_NAME_MINES = "Mines";
	public static final String MODULE_NAME_RANKS = "Ranks";
	
    private List<Module> modules;
    private List<String> disabledModules;
    private File moduleRoot;

    public ModuleManager() {
        modules = new ArrayList<>();
        disabledModules = new ArrayList<>();
        
        moduleRoot = getModuleRootDefault();
    }

    
    public boolean isEnabled( String moduleName ) {
    	
    	Module module = getModule( moduleName );
    	
    	return module != null && module.isEnabled();
    }
    
    public static File getModuleRootDefault() {
    	 File moduleRoot = new File(PrisonAPI.getPluginDirectory(), MODULE_MANAGER_DIRECTORY);
         if (!moduleRoot.exists()) {
             moduleRoot.mkdir();
         }
         
         return moduleRoot;
    }
    
    /**
     * Register a new module.
     */
    public void registerModule(Module module) {
    	
    	if ( module != null ) {
    		
    		// If module already exists, remove it so it can be added:
    		if ( getModule(module.getName()) != null ) {
    			removeModule( module.getName() );
    		}
    		
    		if ( getModule(module.getName()) == null ) {
    			// Module does not exist, so add it:
    			modules.add(module);
    			enableModule(module);
//            return; // Already added
    		}
    	}
    }

    private void validateVersion(Module module) {
        if (module.getApiTarget() == Prison.API_LEVEL) {
            return; // Version matches, no need to continue
        }

        module.getStatus().setMessage("&6Version mismatch (update module)");
        Output.get().logWarn(
            "API level mismatch! " + module.getPackageName() + " is on API " + module.getApiTarget()
                + ", while prison-core is on API " + Prison.API_LEVEL
                + ".\nThis may cause problems.");
    }

    /**
     * Enable an already loaded module.
     *
     * @param module The {@link Module} to enable.
     * @return true if the enable succeeded, false otherwise.
     */
    public boolean enableModule(Module module) {
        long startTime = System.currentTimeMillis();
        Output.get().logInfo("%s Module enablement starting...", module.getName());

        module.setEnabled(true);
        module.enable();
        validateVersion(module);

        if (module.getStatus().getStatus() != ModuleStatus.Status.ENABLED) {
            // Anything else and we assume that the enable failed.
        	
            Output.get().logInfo("%s Module enablement &cfailed&f in %d milliseconds. &d[%s&d]", module.getName(),
                (System.currentTimeMillis() - startTime), module.getStatus().getMessage() );
            return false;
        }

        Output.get().logInfo("%s Module enabled successfully in %d milliseconds.", module.getName(),
            (System.currentTimeMillis() - startTime));
        return true;
    }

    /**
     * Unregister a module. This will disable it and then remove it from the list.
     *
     * @param module The {@link Module} to enable.
     */
    public void unregisterModule(Module module) {
    	
        disableModule(module);
        
        getModules().remove(module);
//        getModule(module.getName()).ifPresent(modules::remove);
    }

    /**
     * Disable an already loaded module.
     *
     * @param module The {@link Module} to disable.
     */
    public void disableModule(Module module) {
        if(!module.isEnabled()) return; // Don't disable enabled modules
        module.disable();
        module.getStatus().toDisabled();
    }

    /**
     * Unregister all modules.
     *
     * @see #unregisterModule(Module)
     */
    public void unregisterAll() {
        modules.forEach(this::disableModule);
        modules.clear();
        
        disabledModules.clear();
    }

    /**
     * Returns the {@link Module} with the specified name.
     */
    public Module getModule(String name) {
    	Module results = null;
    	
    	for (Module module : getModules() ) {
			
    		if ( module.getName().equalsIgnoreCase(name) ) {
    			results = module;
    			break;
    		}
		}
    	return results;
    	
//        return modules.stream().filter(module -> module.getName().equalsIgnoreCase(name))
//            .findFirst();
    }
    
    /**
     * Removes a module, if its already been added, by name.
     * 
     * @param name
     * @return
     */
    public boolean removeModule( String name ) {
    	Module results = null;
    	
    	for (Module module : getModules() ) {
			
    		if ( module.getName().equalsIgnoreCase(name) ) {
    			results = module;
    			break;
    		}
		}
    	
    	return results == null ? false : getModules().remove( results );
    }

    /**
     * Returns the {@link Module} with the specified package name.
     */
    public Module getModuleByPackageName(String name) {
    	Module results = null;
    	
    	for (Module module : getModules() ) {
			
    		if ( module.getPackageName().equalsIgnoreCase(name) ) {
    			results = module;
    			break;
    		}
		}
    	return results;
    	
//        return modules.stream().filter(module -> module.getPackageName().equalsIgnoreCase(name))
//            .findFirst();
    }

    /**
     * Returns a list of all modules.
     */
    public List<Module> getModules() {
        return modules;
    }

    public List<String> getDisabledModules() {
		return disabledModules;
	}

	public File getModuleRoot() {
        return moduleRoot;
    }

//    /**
//     * Returns the status of a module (enabled or error message), in the form of a color-coded string.
//     * This is meant to show to users.
//     *
//     * @deprecated Use {@link Module#getStatus()} instead.
//     */
//    @Deprecated public String getStatus(String moduleName) {
//        Optional<Module> moduleOptional = getModule(moduleName);
//        return moduleOptional.map(module -> module.getStatus().getMessage()).orElse(null);
//    }

//    /**
//     * Set the status of a module.
//     *
//     * @param moduleName The name of the module.
//     * @param newStatus  The module's status. May include color codes, amp-prefixed.
//     * @deprecated Use {@link Module#getStatus()} instead.
//     */
//    @Deprecated public void setStatus(String moduleName, String newStatus) {
//        Optional<Module> moduleOptional = getModule(moduleName);
//        if (!moduleOptional.isPresent()) {
//            return;
//        }
//        Module module = moduleOptional.get();
//
//        if (newStatus.toLowerCase().contains("enabled")) {
//            module.getStatus().toEnabled();
//        } else if (newStatus.toLowerCase().contains("disabled")) {
//            module.getStatus().toDisabled();
//        } else {
//            module.getStatus().toFailed(newStatus);
//        }
//
//    }

	public boolean isModuleActive(String moduleName) {
		boolean results = false;
		
		if ( moduleName != null ) {
			
			Module module = getModule(moduleName);
			if ( module != null ) {
				results = module.getStatus().getStatus() == Status.ENABLED;
			}
		}
		
		return results;
	}
}
