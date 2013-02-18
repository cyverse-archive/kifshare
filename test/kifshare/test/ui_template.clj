(ns kifshare.test.ui-template
  (:use [kifshare.ui-template]
        [midje.sweet]))

(fact "test-unit-test"
      (+ 1 1) => 2)

(fact "Clear div"
      (clear) => "<div class=\"clear\"></div>")

(fact "Section spacer"
      (section-spacer) => "<div class=\"section-spacer\"></div>")

(fact "AVU table row"
      (irods-avu-row {:attr "attr" :value "value" :unit "unit"}) =>
      "<tr><td>attr</td><td>value</td><td>unit</td></tr>")

(fact "AVU table"
      (irods-avu-table [{:attr "attr" :value "value" :unit "unit"}]) =>
      "<div id=\"irods-avus\"><div id=\"irods-avus-header\"><h2>Metadata</h2></div><table id=\"irods-avus-data\"><thead><tr><th>Attribute</th><th>Value</th><th>Unit</th></tr></thead><tbody><tr><td>attr</td><td>value</td><td>unit</td></tr></tbody></table><div class=\"section-spacer\"></div></div>")