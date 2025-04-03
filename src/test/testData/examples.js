// copy this over to the test UI
// only the marked pieces must be highlighted

const Prism = require('prismjs')
import Myname from 'wtf'
import {members, too} from 'ts'

const abb = true

function hello() {
    //     ^highlighted
    function hello05() {}
    //       ^highlighted

    hello05()
}

hello('')

const hello2 = function () {}
//    ^highlighted

hello2('')

const hello3 = function hello() {}
//    ^highlighted      ^highlighted

hello3('')

const hello4 = () => {}
//    ^highlighted

hello4()(
    (function hello5() {})()
    //        ^highlighted
)

function* generator(i) {
    //      ^highlighted
    yield i
    yield i + 10
}

generator(null)

const myPromise = new Promise((resolve, reject) => {
    setTimeout(() => {
        resolve('foo')
    }, 300)
})

myPromise
    .then(handleFulfilledA, handleRejectedA)
    .then(handleFulfilledB, handleRejectedB)
    .then(handleFulfilledC, handleRejectedC)

const obj = {
    fieldFunc: () => {},
    // ^highlighted
    anotherFieldFunc: function hi() {},
    // ^highlighted            ^highlighted
}

obj.fieldFunc()
obj.anotherFieldFunc()

class Rectangle {
    //  ^highlighted
    constructor(height, width) {
        // ^highlighted
        this.height = height
        this.width = width
    }

    someMethod() {}
    // ^highlighted
}

const rect = new Rectangle(1, 2)

rect.someMethod()

const Rectangle2 = class {
    //  ^highlighted
    constructor(height, width) {
        // ^highlighted
        this.height = height
        this.width = width
    }
}

const rect2 = new Rectangle2(1, 2)

const Rectangle3 = class Rectangle2 {
    //  ^highlighted       ^highlighted
    constructor(height, width) {
        // ^highlighted
        this.height = height
        this.width = width
    }
}

new Rectangle3(1, 2)
