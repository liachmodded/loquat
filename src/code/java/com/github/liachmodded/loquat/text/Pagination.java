/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.loquat.text;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.text.Text;

public interface Pagination {

  int getPages();

  Text render(int page);

  interface Builder {

    default Builder lines(Iterable<? extends Text> pages) {
      return lines(pages, Function.identity());
    }

    default Builder lineSuppliers(Iterable<Supplier<? extends Text>> pageSuppliers) {
      return lines(pageSuppliers, Supplier::get);
    }

    <T> Builder lines(Iterable<? extends T> content, Function<? super T, ? extends Text> function);

    Pagination build(PaginationFormat format);
  }

}
