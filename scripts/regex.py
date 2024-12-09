import re
import sys
import os

# Petri Net's Transition Invariants
invariants = {
    "T0 T1 T2 T5 T6 T9 T10 T11": 0,
    "T0 T1 T2 T5 T7 T8 T11": 0,
    "T0 T1 T3 T4 T6 T9 T10 T11": 0,
    "T0 T1 T3 T4 T7 T8 T11": 0
}

# Regular expresion
pattern = r"(T0)(.*?)?(T1)(.*?)?((T2)(.*?)?(T5)|(T3)(.*?)?(T4))(.*?)?((T6)(.*?)?(T9)(.*?)?(T10)|(T7)(.*?)?(T8))(.*?)?(T11)"
sub = r'\g<2>\g<4>\g<7>\g<10>\g<12>\g<15>\g<17>\g<20>\g<22>'

# Path to input file
file_path = "/tmp/transitionsSequence.txt"

def read_file(file_path):
    """Read input file and return content"""
    try:
        with open(file_path, 'r') as file:
            content = file.read().replace("\n", "")  # Delete endline
        return content
    except FileNotFoundError:
        print(f"❌ File {file_path} not found.")
        # Exit program with error
        sys.exit(1)
    
def get_removed_transitions(match, sub):
    """get transitions matches to delete"""
    removed_transitions = []
    for i in range(1, len(match.groups()) + 1):
        # Check if group != None
        if f"\\g<{i}>" not in sub and match.group(i) is not None and i != 5 and i != 13:
            removed_transitions.append(match.group(i))
    
    # Filter transitions duplicates
    return removed_transitions


def update_invariants(transitions):
    """update invariants counter"""
    if transitions in invariants:
        invariants[transitions] +=1
    else:
        print(f"❌ Transition invariant {transitions} not found.")
        # Exit program with error
        sys.exit(1)

def process_matches(content, pattern, sub):
    """ process all transition invariants found in log file"""
    match_num = 0
    
    while True:
        found_match = False
        matches = re.finditer(pattern, content)

        if not matches:
            break # If no matches found, exit while loop

        for match in matches:
            match_num += 1
            removed_transitions = get_removed_transitions(match, sub)

            # Print transitions deleted
            if removed_transitions:
                transition_string = ' '.join(removed_transitions)
                update_invariants(transition_string)
            
            # Update content without found match
            content = re.sub(pattern, sub, content, count=1)

            # Print remaining transitions if content is not empty
            if content.strip():
                print(f"Remaining transitions: {content.strip()}\n")

            match_num += 1
            
            break
        
        if not found_match:
            break

        else:
            print("✅ No more transitions to process.")
            break
        break


    if match_num == 0:
        print("❌ No matches found that match the pattern.")

    return content

def analyze_transitions(file_path):
    """Analyze transitions from file"""

    # Store file content in a variable
    content = read_file(file_path)

    content = process_matches(content, pattern, sub)

    print("\n✅ Complete analysis.")

    if content.strip():
        print(f"\n❌ There were transitions that did not match the pattern: {content.strip()}")
    else:
        print("\n✅ All transitions were processed.")

    print("\nNumber of Invariant:")
    for invariant, count in invariants.items():
        print(f"{invariant}: {count} times")

def calculate_percentage_from_invariants(invariants):
    counts = {"T2": 0, "T3": 0, "T6": 0, "T7": 0}

    for invariant, frec in invariants.items():
        if "T2" in invariant:
            counts["T2"] += frec
        if "T3" in invariant:
            counts["T3"] += frec
        if "T6" in invariant:
            counts["T6"] += frec
        if "T7" in invariant:
            counts["T7"] += frec

    total_T2_T3 = counts["T2"] + counts["T3"]
    total_T6_T7 = counts["T6"] + counts["T7"]

    if total_T2_T3 > 0:
        T2_percentage = (counts["T2"] / total_T2_T3) * 100
        T3_percentage = (counts["T3"] / total_T2_T3) * 100
    else:
        T2_percentage = T3_percentage = 0

    if total_T6_T7 > 0:
        T6_percentage = (counts["T6"] / total_T6_T7) * 100
        T7_percentage = (counts["T7"] / total_T6_T7) * 100
    else:
        T6_percentage = T7_percentage = 0

    print("Percentages calculated from invariants:")
    print(f"T2: {T2_percentage:.2f}%↑       T6: {T6_percentage:.2f}%↑")
    print(f"T3: {T3_percentage:.2f}%↓       T7: {T7_percentage:.2f}%↓")   
   

# RUN PROGRAM
analyze_transitions(file_path)
calculate_percentage_from_invariants(invariants)

# Delete transitions file
os.remove(file_path)
