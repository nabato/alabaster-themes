package main

import (
	"fmt"
	_ "os"
	_ "os/exec"
	_ "strings"
	_ "time"
)

// var foo int // declaration without initialization
// var foo int = 42 // declaration with initialization
// var foo, bar int = 42, 1302 // declare and init multiple vars at once
var foo = 42 // type omitted, will be inferred
// foo := 42 // shorthand, only in func bodies, omit var keyword, type is always implicit
const constant = "This is a constant"

func main() {
	fmt.Println(foo, foo) // 1 2 (0 is skipped)
	fmt.Println(foo, foo) // 8 16 (2^3, 2^4)
	fmt.Println('a')      // 8 16 (2^3, 2^4)
	boolie := true
	boolie2 := false
	boolie3 := 0

	boolie3 = 2
	boolie = true
	boolie2 = false
}

// a simple function
func functionName() {}

// function with parameters (again, types go after identifiers)
func functionNamLe2(param1 string, param2 int) {}

// multiple parameters of the same type
func functionName3(param1, param2 int) {}

// return type declaration
func functionName4() int {
	return 42
}

// Can return multiple values at once
func returnMulti() (int, string) {
	return 42, "foobar"
}

var num, stringy = returnMulti()

// Return multiple named results simply by return
func returnMulti2() (n int, s string) {
	n = 42.0
	_ = 42.1
	s = "foobar"
	// n and s will be returned
	return
}

var x, str = returnMulti2()
