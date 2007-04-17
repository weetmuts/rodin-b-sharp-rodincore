package org.eventb.core.seqprover;


import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eventb.internal.core.seqprover.ReasonerRegistry;
import org.eventb.internal.core.seqprover.TacticRegistry;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class SequentProver extends Plugin {

	public static final String PLUGIN_ID = "org.eventb.core.seqprover"; //$NON-NLS-1$
	
	/**
	 * debugging/tracing option names
	 */
	private static final String SEQPROVER_TRACE = PLUGIN_ID + "/debug/seqProver"; //$NON-NLS-1$
	private static final String REASONER_REGISTRY_TRACE = PLUGIN_ID + "/debug/reasonerRegistry"; //$NON-NLS-1$	
	private static final String TACTIC_REGISTRY_TRACE = PLUGIN_ID + "/debug/tacticRegistry"; //$NON-NLS-1$
	
	/**
	 * The shared instance.
	 */
	private static SequentProver plugin;

	/**
	 * Debug flag for <code>SEQPROVER_TRACE</code>
	 */
	private static boolean DEBUG;
	
	/**
	 * Creates the Sequent Prover plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform.
	 * Clients must not call.
	 * </p>
	 */
	public SequentProver() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		configureDebugOptions();
	}

	/**
	 * Process debugging/tracing options coming from Eclipse.
	 */
	private void configureDebugOptions() {
		if (isDebugging()) {
			String option;
			option = Platform.getDebugOption(SEQPROVER_TRACE);
			if (option != null)
				SequentProver.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			option = Platform.getDebugOption(REASONER_REGISTRY_TRACE);
			if (option != null)
				ReasonerRegistry.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
			option = Platform.getDebugOption(TACTIC_REGISTRY_TRACE);
			if (option != null)
				TacticRegistry.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static SequentProver getDefault() {
		return plugin;
	}

	public static IReasonerRegistry getReasonerRegistry(){
		return ReasonerRegistry.getReasonerRegistry();
	}
	
	public static ITacticRegistry getTacticRegistry(){
		return TacticRegistry.getTacticRegistry();
	}
	
	public static void debugOut(String message){
		if (DEBUG)
			System.out.println(message);
	}
	
}
