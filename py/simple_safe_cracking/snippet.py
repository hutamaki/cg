
msg = input()
litteral = ['zero', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine']

alphabet = [x for x in "abcdefghijklmnopqrstuvwxyz"]
msgNumbers = [ord(x) for x in msg]

shift = ord(msg[0].lower()) - ord('t')
decoded = [x if not x.isalnum() else alphabet[(alphabet.index(x.lower()) - shift) % 26] for x in msg]
    
numbers=(''.join(decoded).split()[-1]).split('-')
result = [str(litteral.index(x)) for x in numbers]
print(''.join(result))    

