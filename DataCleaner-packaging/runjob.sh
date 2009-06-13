#!/bin/sh
#  This file is part of DataCleaner.
#
#  DataCleaner is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  DataCleaner is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.

cd `dirname $0`
DATACLEANER_HOME=`pwd`
export DATACLEANER_HOME
echo "Using DATACLEANER_HOME: $DATACLEANER_HOME"

JAVA_OPTS="$JAVA_OPTS -Xmx1024m -XX:MaxPermSize=256m"

exec java $JAVA_OPTS -cp .:datacleaner.jar dk.eobjects.datacleaner.gui.DataCleanerCli $*