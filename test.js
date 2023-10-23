// Выделяются синим имена всех callable переменных во время объявления. Но не во время вызова.
// В TS добавлен слегка зеленоватый для типов. Это противоречит концепции, но с ним лучше.

function hello(someStuff){ // правильно
    function hello05(){} // правильно

    hello05() // не должно подсвечиваться

    return someStuff + " from a function."
}

hello("hello") // не должно подсвечиваться

const hello2 = function(){} // правильно

hello2("") // не должно подсвечиваться

const hello3 = function hello(){} // правильно

hello3("") // не должно подсвечиваться

const hello4 = () => {} // должно быть синим

hello4() // не должно подсвечиваться

// ( function hello5(){ } () ) // правильно

function* generator(i) { // правильно. генераторы такие же функции, async тоже просто функции, также промисы.
    yield i;
    yield i + 10;
}

generator(null) //не должно подсвечиваться

const myPromise = new Promise((resolve, reject) => { // правильно
    setTimeout(() => { //не должно подсвечиваться
        resolve("foo");
    }, 300);
});

myPromise //правильно
    .then(null, null)
    .then(null, null)
    .then(null, null);


const obj = {
    fieldFunc: () => {}, // fieldFunc должен быть синим.
    anotherFieldFunc: function hi() {} // anotherFieldFunc и hi должны быть синими.
}

obj.fieldFunc() //правильно
obj.anotherFieldFunc() //правильно

class Rectangle {  // правильно, должен быть синим (почему-то сейчас чуть светлее нужного)
    constructor(height, width) {
        this.height = height;
        this.width = width;
    }

    someMethod() {} // должно быть синим. также и arrow func должна быть синей.
}

const rect = new Rectangle(1,2) //не должно подсвечиваться

rect.someMethod() // правильно

const Rectangle2 = class { // правильно
    constructor(height, width) {// правильно
        this.height = height;
        this.width = width;
    }
};

const rect2 = new Rectangle2(1,2) // правильно


const Rectangle3 = class Rectangle2 { // правильно
    constructor(height, width) {
        this.height = height;
        this.width = width;
    }
};

new Rectangle3(1,2 ) //правильно

