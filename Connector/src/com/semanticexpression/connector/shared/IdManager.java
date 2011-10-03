// Copyright 2011 Semantic Expression, Inc. All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the GNU General Public License, either version 3 or (at your option)
// any later version. The terms of this license may be found at
// http://www.gnu.org/copyleft/gpl.html
//
// This program is made available on an "as is" basis, without warranties or
// conditions of any kind, either express or implied.
//
// Please contact us for other licensing options.
//
// Contributors:
//
// Anthony F. Stuart - Initial implementation
//
//
//

package com.semanticexpression.connector.shared;

import java.util.Random;

/**
 * Efficiently manages the allocation of content identifiers in a distributed
 * environment without centralized coordination.
 * <p/>
 * Implementation Notes:
 * <ul>
 * <li>For background information on the challenges and approaches to generating
 * unique identifiers, please read <a href=
 * 'http://en.wikipedia.org/wiki/Universally_unique_identifier'>
 * http://en.wikipedia.org/wiki/Universally_unique_identifier. </a></li>
 * <li>This system uses 64 bit identifiers. The choice of length is a trade-off
 * between the probability of unintentionally generating a duplicate identifier
 * and the ease of handling 64 bit integers by both software and users (when
 * formatted as four groups of four hexadecimal digits).</li>
 * <li>Assuming the random number generator is properly seeded and produces a
 * high statistical dispersion, the probability that a generated identifier will
 * match one that has already been generated is 1 in 2^64 or 1 in
 * 18,446,744,073,709,551,614. If we generate 1,000 identifiers a second, it
 * would take 584,542,046 years to generate 2^64 identifiers.</li>
 * <li>If the assumption about the random number generator is not correct, then
 * generating longer sequences may not increase the probability of uniqueness.</li>
 * <li>Practically speaking this class doesn't need to be perfect. It only needs
 * to generate an identifier that is "unique enough" that the probability of an
 * unintentional duplicate is lower than the probability that some other type of
 * bug in the system will produce a similar effect.</li>
 * <li>Just because we can measure the statistical likelihood of a duplicate
 * does not mean that this class deserves more attention than other parts of the
 * system where we can't measure its correctness.</li>
 */
public final class IdManager
{
  private static Random random = new Random();

  /**
   * Creates an identifier with a high probability of uniqueness. The returned
   * identifier is not guaranteed to be unique. Programs that use the identifier
   * should fail gracefully if the identifier subsequently proves not to be
   * unique.
   */
  public static Id createIdentifier()
  {
    return new Id(random.nextLong());
  }

}
