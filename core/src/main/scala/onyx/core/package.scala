package onyx

import onyx.core._

package object core {
  object implicits extends ChainableImplicits
    with syntax.implicits
}