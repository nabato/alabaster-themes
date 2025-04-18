# frozen_string_literal: true

str = 'hello' # strings are highlighted

puts str

num = 5 # numbers are highlighted
constt = true # no way to highlight only boolean constants

# All callable variables should be highlighted during declaration, but not during invocation.

def hello(some_stuff) # highlighted
  def hello05 # highlighted
  end

  hello05() # should not be highlighted

  return some_stuff + ' from a function.'
end

hello('hello') # should not be highlighted

hello2 = lambda do # no way to highlight lambda declarations
end

hello2.call('') # should not be highlighted

hello3 = -> {
} # no way to highlight short lambda declarations

hello3.call # should not be highlighted

def generator(i) # highlighted
  Enumerator.new do |yielder|
    yielder << i
    yielder << (i + 10)
  end
end

generator(nil) # should not be highlighted

obj = {
  field_func: -> {
  } # no way to highlight lambda declarations
}

class Rectangle # highlighted
  def initialize(height, width) # highlighted
    @height = height
    @width = width
  end

  def some_method # highlighted
  end
end

rect = Rectangle.new(1, 2) # should not be highlighted

rect.some_method # should not be highlighted

Rectangle2 = Class.new do # highlighted
  def initialize(height, width) # highlighted
    @height = height
    @width = width
  end
end

rect2 = Rectangle2.new(1, 2) # should not be highlighted
