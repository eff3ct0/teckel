package com.eff3ct.teckel.semantic

import com.eff3ct.teckel.model.{Asset, Context}

package object core {
  type EvalAsset[+T] = Semantic[Asset, Context[Asset], T]
}
