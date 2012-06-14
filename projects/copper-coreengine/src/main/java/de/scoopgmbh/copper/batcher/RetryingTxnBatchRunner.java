package de.scoopgmbh.copper.batcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.db.utility.RetryingTransaction;

public class RetryingTxnBatchRunner<E extends BatchExecutorBase<E,T>, T extends BatchCommand<E,T>> implements BatchRunner<E, T> {

	private static final Logger logger = LoggerFactory.getLogger(RetryingTxnBatchRunner.class);

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void run(final Collection<BatchCommand<E,T>> commands, final BatchExecutorBase<E,T> base) {
		if (commands.isEmpty())
			return;

		try {
			if (dataSource == null) {
				base.doExec(commands, null);
			}
			else {
				new RetryingTransaction(dataSource) {
					@Override
					protected void execute() throws Exception {
						base.doExec(commands, getConnection());
					}
				}.run();
			}
			for (BatchCommand<?,?> cmd : commands) {
				cmd.callback().commandCompleted();
			}
		} 
		catch (Exception e) {
			if (commands.size() == 1) {
				BatchCommand<?,?> cmd = commands.iterator().next();
				cmd.callback().unhandledException(e);
			}
			else {
				logger.warn("batch execution failed - trying execution of separate commands ",e);
				for (BatchCommand<E,T> cmd : commands) {
					List<BatchCommand<E,T>> l = new ArrayList<BatchCommand<E,T>>();
					l.add(cmd);
					run(l, base);
				}
			}
		}
	}

}
