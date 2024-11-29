import re
import sys

# Invatiants Transitions for Our Petri Net
invariants = {
    "T0 T1 T2 T5 T6 T9 T10 T11": 0,
    "T0 T1 T2 T5 T7 T8 T11": 0,
    "T0 T1 T3 T4 T6 T9 T10 T11": 0,
    "T0 T1 T3 T4 T7 T8 T11": 0
}

# Regular expresion
pattern = r"(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)"
sub = r'\g<2>\g<4>\g<7>\g<10>\g<12>\g<15>\g<17>\g<20>\g<22>'

# Path to .txt file
file_path = "/tmp/transitionsSequence.txt"

def read_file(file_path):
    try:
        with open(file_path, 'r') as file:
            content = file.read().replace("\n", "")  # delete endline
        print(f"Transitions read from file: {content}\n")
        return content
    except FileNotFoundError:
        print(f"❌ File {file_path} not found.")
        # Exit program with error
        sys.exit(1)
        
    
def get_removed_transitions(match, sub):
    """get transitions matches to delete"""
    removed_transitions = []
    for i in range(1, len(match.groups()) + 1):
        # verify if group i = None
        if f"\\g<{i}>" not in sub and match.group(i) is not None and i != 5 and i != 13:
            removed_transitions.append(match.group(i))
    
    # Filter transitions duplicates
    return removed_transitions


def update_invariants(transitions):
    """update invariants counter"""
    if transitions in invariants:
        invariants[transitions] +=1

def process_matchs(content, pattern, sub):
    """ process all transition invariants found in log file"""
    match_num= 1
    while True:
        matches = re.finditer(pattern, content)
        found_match = False  

        for m in matches:
            found_match = True
            removed_transitions = get_removed_transitions(m, sub)

            # Print transitions deleted
            if removed_transitions:
                print(f"Match {match_num}: {' '.join(removed_transitions)}")
                transition_string = ' '.join(removed_transitions)
                update_invariants(transition_string)
            
            # Update old content
            content = re.sub(pattern, sub, content, count=1)
            # Print remaining transitions if content is not empty
            if content.strip():
                print(f"Remaining transitions: {content.strip()}\n")
            match_num += 1
        
        if not found_match:
            break

    if match_num == 1:
        print("❌ No matches found that match the pattern.")

    return content

def analyze_transitions(file_path):

    content = read_file(file_path)

    content = process_matchs(content, pattern, sub)

    print("\n✅ Complete analysis.")
    if content.strip():
        print(f"\n❌ There were transitions that did not match the pattern: {content.strip()}")
    else:
        print("\n✅ All transitions were processed.")

    print("\nNumber of Invariant:")
    for invariant, count in invariants.items():
        print(f"{invariant}: {count} times")


# RUN PROGRAM
analyze_transitions(file_path)
