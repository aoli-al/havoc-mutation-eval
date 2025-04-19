import sys
import pandas as pd
from process_data import process_mutation_data

mutation_df = process_mutation_data(sys.argv[1], [False, True], [
                        "random", "zest-mini", "zest", "ei", "bedivfuzz-structure", "zeugma-linked"], 'mutations')
new_label_names = {
    'zest': 'Zest',
    'zest-saved_only': 'Zest-saved',
    'ei': 'EI',
    'ei-saved_only': 'EI-saved',
    'zeugma-linked': 'Zeugma',
    'zeugma-linked-saved_only': 'Zeugma-saved',
    'bedivfuzz-structure': 'BeDivFuzz',
    'bedivfuzz-structure-saved_only': 'BeDivFuzz-saved',
    'random-saved_only': 'Random-saved',
    'random': 'Random',
    'zest-mini': 'Zest-Mini',
    'zest-mini-saved_only': 'Zest-Mini-saved'
}
mutation_df['algorithm'] = mutation_df['algorithm'].map(new_label_names)
mutation_df['mutation_distance_diff'] = mutation_df['mutation_string'] - \
    mutation_df['mutation_bytes']
mutation_df.to_csv(sys.argv[2] + "/mutation_distances.csv", index=False)
