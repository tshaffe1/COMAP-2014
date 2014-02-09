#!/usr/bin/env python
# copyright 2014 Tim Shaffer↩

# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

import math

ERROR = 1e-5
RESOLUTION = 1e2
N = 128
AREA = 1/2 * math.sqrt(math.pi) * math.erf(5)
def f(x):
    return math.e**(-x**2)

def area(x1, x2):
    return abs(f(x1) - f(x2)) * max(abs(x1), abs(x2))
def epsilon(x1, x2):
    return abs((AREA / N) - area(x1, x2))

x = 0
edges_x = [0]
edges_y = [1]
for i in range(N - 1):
    while epsilon(x, edges_x[-1]) > ERROR:
        x += ERROR / RESOLUTION
    edges_x.append(x)
    edges_y.append(f(x))
    print('found bound %i' % i)

