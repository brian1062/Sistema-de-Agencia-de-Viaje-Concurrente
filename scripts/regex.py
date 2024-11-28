import re

# Invatiants Transitions for Our Petri Net
invariants = {
    "T0 T1 T2 T5 T6 T9 T10 T11": 0,
    "T0 T1 T2 T5 T7 T8 T11": 0,
    "T0 T1 T3 T4 T6 T9 T10 T11": 0,
    "T0 T1 T3 T4 T7 T8 T11": 0
}

# Regular expresion
pattern = r"(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)(.*?)"
sub = r'\g<2>\g<4>\g<7>\g<10>\g<12>\g<15>\g<17>\g<20>\g<22>\g<24>'

# Path to .txt file
file_path = "transitions.txt"

def read_file(file_path):
    try:
        with open(file_path, 'r') as file:
            content = file.read().replace("\n", "")  # delete endline
        print(f"Transitions read from file: {content}\n")
        return content
    except FileNotFoundError:
        print(f"❌ File {file_path} not found.")
        return None
    
def get_removed_transitions(match, sub):
    """get transitions matches to delete"""
    removed_transitions = []
    for i in range(1, len(match.groups()) + 1):
        # verify if group i = None
        if f"\\g<{i}>" not in sub and match.group(i) is not None:
            removed_transitions.append(match.group(i))
    
    # Filter transitions duplicates
    return filter_duplicate_transitions(removed_transitions)

def filter_duplicate_transitions(removed_transitions):
    filtered_removed_transitions = []
    for item in removed_transitions:
        transition = re.findall(r'T\d+', item) #get individual transition from T0 T1 T3T4....
        if len(transition) == 1:
            filtered_removed_transitions.append(transition[0])
    
    #delete duplicate transitions
    seen = set()
    ordered_removed_transitions = []
    for item in filtered_removed_transitions:
        if item not in seen:
            ordered_removed_transitions.append(item)
            seen.add(item)
    
    return ordered_removed_transitions

#update invariants counter
def update_invariants(transitions):
    if transitions in invariants:
        invariants[transitions] +=1

def process_matchs(content, pattern, sub):
    match_num= 1
    while True:
        matches = re.finditer(pattern, content)
        found_match = False  

        for m in matches:
            found_match = True
            removed_transitions = get_removed_transitions(m, sub)

            #print transitions deleted
            if removed_transitions:
                print(f"Match {match_num}: {' '.join(removed_transitions)}")
                transition_string = ' '.join(removed_transitions)
                update_invariants(transition_string)
            
            # Update old content
            content = re.sub(pattern, sub, content, count=1)
            print(f"Remaining transitions: {content.strip()}\n")
            match_num += 1
        
        if not found_match:
            break

    if match_num == 1:
        print("❌ No matches found that match the pattern.")

    return content




def analyze_transitions(file_path):
    content = read_file(file_path)
    if content is None: ##Error reading file
        return
    

    content = process_matchs(content, pattern, sub)

    print("✅ Complete analysis.")
    if content.strip():
        print(f"⚠️ There were transitions that do not match the pattern: {content.strip()}")
    else:
        print("🔄 All transitions were processed.")

    print("\nNumber of Invariant:")
    for invariant, count in invariants.items():
        print(f"{invariant}: {count} times")



# RUN PROGRAM
analyze_transitions(file_path)
