import re

# Define the complete regular expression
pattern = r"(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)(.*?)"
sub = r'\g<2>\g<4>\g<7>\g<10>\g<12>\g<15>\g<17>\g<20>\g<22>\g<24>'

def analyze_transitions(file_path):
    try:
        # Read the text file
        with open(file_path, 'r') as file:
            content = file.read().replace("\n", "")  # delete endline
        print(f"Transitions read from file: {content}\n")
        
        match_num = 1
        while True:
            # Find and delete match
            new_content, num = re.subn(
                pattern,  # regular expression
                sub,
                content,  # content
                count=1  # Replace only the first match
            )
            
            if num == 0:  # if no match
                break
            

            print(f"Match {match_num}: {new_content.strip()}")
            
            # Update old content
            content = new_content.replace(f" MATCH_{match_num} ", "", 1)
            print(f"Remaining transitions: {content.strip()}\n")
            
            match_num += 1
        
        # Show final status
        if match_num == 1:
            print("❌ No matches found that match the pattern.")
        else:
            print("✅ Complete analysis.")
            if content.strip():
                print(f"⚠️ There were transitions that do not match the pattern: {content.strip()}")
            else:
                print("🔄 All transitions were processed.")
    
    except FileNotFoundError:
        print(f"❌ File {file_path} no found.")
    except Exception as e:
        print(f"⚠️ excepcion: {e}")

# Path to .txt file
file_path = "transitions.txt"

# Call the function
analyze_transitions(file_path)