import "date"

sum_result = from(bucket: "fuzzer")
  |> range(start: 0, stop: date.add(d: 1d, to: time(v: 0)))
  |> filter(fn: (r) => r["_measurement"] == "coverage")
  |> filter(fn: (r) => r["_field"] == "covered")
  |> filter(fn: (r) => r["experiment"] == "test1")
  |> filter(fn: (r) => r["fuzzer"] == "zest")
  |> filter(fn: (r) => r["testClassName"] == "edu.berkeley.cs.jqf.examples.closure.CompilerTest")
  |> filter(fn: (r) => r["testMethodName"] == "testWithGenerator")
  |> aggregateWindow(every: 2s, fn: count)
  |> cumulativeSum()

sum_result
  |> aggregateWindow(every: 2s, fn: mean, createEmpty: false)
  |> yield(name: "mean")

sum_result
  |> aggregateWindow(every: 2s, fn: max, createEmpty: false)
  |> yield(name: "max")

sum_result
  |> aggregateWindow(every: 2s, fn: min, createEmpty: false)
  |> yield(name: "min")

from(bucket: "fuzzer")
  |> range(start: 0, stop: date.add(d: 1d, to: time(v: 0)))
  |> filter(fn: (r) => r["_measurement"] == "coverage")
  |> filter(fn: (r) => r["_field"] == "covered")
  |> filter(fn: (r) => r["experiment"] == "test2")
  |> filter(fn: (r) => r["fuzzer"] == "zest")
  |> filter(fn: (r) => r["testClassName"] == "edu.berkeley.cs.jqf.examples.closure.CompilerTest")
  |> filter(fn: (r) => r["testMethodName"] == "testWithGenerator")
  |> aggregateWindow(every: 2s, fn: count)
  |> cumulativeSum()
  |> yield(name: "sum")


import "date"

from(bucket: "test_bucket")
  |> range(start: 0, stop: date.add(d: 1d, to: time(v: 0)))
//   |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "coverage")
  |> filter(fn: (r) => r["_field"] == "value")
  |> filter(fn: (r) => r["experiment"] == "test1")
  |> filter(fn: (r) => r["fuzzer"] == "zest")
  |> filter(fn: (r) => r["testClassName"] == "edu.berkeley.cs.jqf.examples.closure.CompilerTest")
  |> filter(fn: (r) => r["testMethodName"] == "testWithGenerator")
  |> group(columns: ["experiment", "fuzzer", "testClassName", "testMethodName"])
  |> aggregateWindow(every: 2s, fn: count)
  |> cumulativeSum()
  |> yield(name: "sum")


import "date"

from(bucket: "test_bucket2")
  |> range(start: 0, stop: date.add(d: 20m, to: time(v: 0)))
//   |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "coverage")
  |> filter(fn: (r) => r["_field"] == "total")
  |> filter(fn: (r) => r["experiment"] == "test1")
  |> filter(fn: (r) => r["fuzzer"] == "zest")
  |> filter(fn: (r) => r["testClassName"] == "edu.berkeley.cs.jqf.examples.closure.CompilerTest")
  |> filter(fn: (r) => r["testMethodName"] == "testWithGenerator")
  |> yield(name: "sum")


import "date"

sum_result = from(bucket: "test_bucket")
  |> range(start: 0, stop: date.add(d: 20m, to: time(v: 0)))
//   |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "coverage")
  |> filter(fn: (r) => r["_field"] == "total")
  |> filter(fn: (r) => r["experiment"] == "test1")
  |> filter(fn: (r) => r["fuzzer"] == "zest")
  |> filter(fn: (r) => r["testClassName"] == "edu.berkeley.cs.jqf.examples.closure.CompilerTest")
  |> filter(fn: (r) => r["testMethodName"] == "testWithGenerator")

sum_result
  |> group(columns: ["experiment"])
  |> aggregateWindow(every: 2s, fn: mean, createEmpty: false)
  |> yield(name: "mean")

sum_result
  |> group(columns: ["experiment"])
  |> aggregateWindow(every: 2s, fn: max, createEmpty: false)
  |> yield(name: "max")

sum_result
  |> group(columns: ["experiment"])
  |> aggregateWindow(every: 2s, fn: min, createEmpty: false)
  |> yield(name: "min")



import "date"
import "interpolate"

sum_result = from(bucket: "influxdb-t1")
  |> range(start: 0, stop: date.add(d: 5m, to: time(v: 0)))
//   |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "coverage")
  |> filter(fn: (r) => r["_field"] == "total")
  |> filter(fn: (r) => r["experiment"] == "influxdb-t1")
  |> filter(fn: (r) => r["fuzzer"] == "zest")
  |> filter(fn: (r) => r["testClassName"] == "edu.berkeley.cs.jqf.examples.closure.CompilerTest")
  |> filter(fn: (r) => r["testMethodName"] == "testWithGenerator")
  |> toFloat()
  |> fill(usePrevious: true)
  |> interpolate.linear(every: 1m)
  |> aggregateWindow(every: 1m, fn: mean, createEmpty: false)

//
sum_result
  |> yield(name: "raw")

sum_result
  |> group(columns: ["experiment"])
  |> aggregateWindow(every: 1m, fn: mean, createEmpty: false)
  |> yield(name: "mean")


sum_result
  |> group(columns: ["experiment"])
  |> aggregateWindow(every: 1m, fn: max, createEmpty: false)
  |> yield(name: "max")

sum_result
  |> group(columns: ["experiment"])
  |> aggregateWindow(every: 1m, fn: min, createEmpty: false)
  |> yield(name: "min")


  import "date"
import "interpolate"

sum_result = from(bucket: "influxdb-t1")
  |> range(start: 0, stop: date.add(d: 1h, to: time(v: 0)))
//   |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "coverage")
  |> filter(fn: (r) => r["_field"] == "total")
  |> filter(fn: (r) => r["experiment"] == "influxdb-t1")
  |> filter(fn: (r) => r["testClassName"] == "edu.berkeley.cs.jqf.examples.closure.CompilerTest")
  |> toFloat()
  |> interpolate.linear(every: 1m)
  |> aggregateWindow(every: 1m, fn: mean, createEmpty: false)
  |> group(columns: ["testMethodName", "fuzzer"])
  |> sort(columns: ["_time"])

//

// sum_result
//   |> yield(name: "data")

sum_result
  |> aggregateWindow(every: 2m, fn: mean, createEmpty: false)
  |> sort(columns: ["_time"])
  |> yield(name: "mean")


sum_result
  |> aggregateWindow(every: 2m, fn: max, createEmpty: false)
    |> sort(columns: ["_time"])
  |> yield(name: "max")

sum_result
  |> aggregateWindow(every: 2m, fn: min, createEmpty: false)
    |> sort(columns: ["_time"])
  |> yield(name: "min")