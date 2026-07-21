package mwerunner;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import org.emoflon.gips.core.milp.SolverOutput;

import mwegipsl.api.gips.MwegipslGipsAPI;
import mwemetamodel.MweModelGenerator;
import mwerunner.utils.LoggerUtils;
import mwerunner.utils.XmiSetupUtil;

public class MweHeadlessRunner {

	/**
	 * Logger for system outputs.
	 */
	protected static final Logger logger = Logger.getLogger(MweHeadlessRunner.class.getName());

	/**
	 * Input model path.
	 */
	private static final String inputModelPath = "./initial_model.xmi";

	/**
	 * Output model path.
	 */
	private static final String outputModelPath = "./optimized_model.xmi";

	/**
	 * Main method to run the complete project. First argument is the number of
	 * guest nodes to generate and second argument is the number of host nodes to
	 * generate.
	 * 
	 * @param args Arguments (see above).
	 */
	public static void main(final String[] args) {
		Objects.requireNonNull(args);
		if (args.length != 2) {
			throw new IllegalArgumentException("Number of given arguments must be equal to 2.");
		}

		// Configure logging
		LoggerUtils.configureLogging(logger);

		// Parse arguments
		// 0: number of guests to generate
		// 1: number of hosts to generate
		final int numberOfGuests = Integer.valueOf(args[0]);
		final int numberOfHosts = Integer.valueOf(args[1]);

		// Generate the corresponding model
		final MweModelGenerator gen = new MweModelGenerator();
		for (int i = 0; i < numberOfGuests; i++) {
			gen.genGuestNode("guest_" + i);
		}
		for (int j = 0; j < numberOfHosts; j++) {
			// We allow one guest per host for now
			gen.genHostNode("host_" + j, 1);
		}

		// Persist initial model
		gen.persistModel(inputModelPath);

		// Initialize GIPS
		final MwegipslGipsAPI api = new MwegipslGipsAPI();
		XmiSetupUtil.checkIfEclipseOrJarSetup(api, inputModelPath);

		// Build MILP problem
		api.buildProblem(true, true);

		// Solve MILP problem
		try (final SolverOutput output = api.solveProblemTimed()) {
			if (output.solutionCount() == 0) {
				api.terminate();
				logger.warning("No solution found. Aborting.");
				throw new InternalError("No solution found!");
			}
			logger.info("=> Objective value: " + output.objectiveValue());
		} catch (final OutOfMemoryError err) {
			logger.warning("GIPS solving threw an OOM error. GIPS now terminates the Java process.");
			System.exit(1);
		}

		// Apply solution with GIPS
		api.applyAllNonZeroMappings();

		// Persist model
		try {
			api.saveResult(outputModelPath);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Finish
		logger.info("GIPS run finished.");
		System.exit(0);
	}

}
