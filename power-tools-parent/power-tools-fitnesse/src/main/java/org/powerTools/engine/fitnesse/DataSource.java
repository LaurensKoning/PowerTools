/*	Copyright 2012 by Martin Gijsen (www.DeAnalist.nl)
 *
 *	This file is part of the PowerTools engine.
 *
 *	The PowerTools engine is free software: you can redistribute it and/or
 *	modify it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the License,
 *	or (at your option) any later version.
 *
 *	The PowerTools engine is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *	GNU Affero General Public License for more details.
 *
 *	You should have received a copy of the GNU Affero General Public License
 *	along with the PowerTools engine. If not, see <http://www.gnu.org/licenses/>.
 */

package org.powerTools.engine.fitnesse;

import java.util.ArrayList;
import java.util.List;

import org.powerTools.engine.sources.TestLineImpl;
import org.powerTools.engine.symbol.Scope;

import fit.Fixture;
import fit.Parse;


final class DataSource extends BaseTestSource {
	private String mInstructionName;
	
	DataSource (Fixture fixture, Parse table, String logFilePath) {
		super (Scope.getGlobalScope (), fixture, table.parts, logFilePath);
	}


	@Override
	public void initialize () {
		processFixtureLine ();
		processHeaderLine ();
	}

	private void processHeaderLine () {
		mRow = mRow.more;
		fillDataTestLine ();
		skipLinkingToLogFile ();
		mPublisher.publishTestLine (mTestLine);

		final int nrOfParts						= mTestLine.getNrOfParts ();
		final StringBuffer instructionNameSb	= new StringBuffer ();
		for (int partNr = 1; partNr < nrOfParts; ++partNr) {
			final String part = mTestLine.getPart (partNr);
			if (part.isEmpty ()) {
				mPublisher.publishError ("empty column name(s)");
				break;
			} else {
				for (String word : part.split (" +")) {
					instructionNameSb.append (word).append (" ");
				}
				instructionNameSb.append ("_ ");
			}
		}
		mPublisher.publishEndOfTestLine ();
		
		mInstructionName = instructionNameSb.toString ().trim ();
	}
	
	private void fillDataTestLine () {
		final List<String> parts = new ArrayList<String> ();
		parts.add ("");
		Parse currentCell = mRow.parts;
		do {
			parts.add (currentCell.text ());
			currentCell = currentCell.more;
		} while (currentCell != null);
		mTestLine.setParts (parts);
	}

	@Override
	public TestLineImpl getTestLine () {
		while ((mRow = mRow.more) != null) {
			fillDataTestLine ();
			mTestLine.setPart (0, mInstructionName);
			linkToLogFile (mRow.parts);
			return mTestLine;
		}
		
		return null;
	}
}