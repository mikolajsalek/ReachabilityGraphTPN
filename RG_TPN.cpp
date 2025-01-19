#include <iostream>
#include <vector>
#include <string>
#include <iomanip>
#include <stdexcept>
#include <utility>
#include <cstdlib>
#include <limits>
#include <map>
#include <algorithm>
#include <iterator>
#include <unordered_set>
#include <sstream>
#include "nlohmann/json.hpp"



using namespace std;

using json = nlohmann::json;

class State{
public:
    int state_name;
    vector<int> p_marking;
    vector<int> t_clock;

    State() : state_name(), p_marking(), t_clock() {}

    State(int name, vector<int> p_m, vector<int> t_cl) : state_name(name), p_marking(p_m), t_clock(t_cl) {}
    
    friend ostream& operator<<(ostream& os, const State& st);
    friend bool operator==(const State& lhs, const State& rhs);

    json to_json() const {
        return {
            {"state_name", state_name},
            {"p_marking", p_marking},
            {"to_clock", t_clock}
        };
    }

};

void to_json(json& j, const State& st){
    j = st.to_json();
}

ostream& operator<<(ostream& os, const State& st){

    string p_marking_to_print;
    string t_clock_to_print;

    for(auto & i: st.p_marking){
        p_marking_to_print += to_string(i) + " ";
    }

    for(auto & i: st.t_clock){
        t_clock_to_print += to_string(i) + " ";
    }

    os << "Nazwa: " << st.state_name << " " << "P_marking: " << p_marking_to_print << " " << "Zegar: " << t_clock_to_print;
    return os;

}

bool operator==(const State& lhs, const State& rhs) {
    return lhs.p_marking == rhs.p_marking && lhs.t_clock == rhs.t_clock;
}

class Edge{
public:
    State old_state;
    State new_state;
    int time;
    string transition;

    Edge() : old_state(), new_state(), time(), transition("") {}

    Edge(State old_s, State new_s, int t, string trn) : old_state(old_s), new_state(new_s), time(t), transition(trn) {}

    json to_json() const {
        return {
            {"old_state", old_state.to_json()},
            {"new_state", new_state.to_json()},
            {"time", time},
            {"transition", transition}
        };
    }

    friend ostream& operator<<(ostream& os, const State& st);

};

ostream& operator<<(ostream& os, const Edge& ed){

    os << ed.old_state << " --- " << ed.time << ", " << ed.transition << " ---> " << ed.new_state << "\n";
    return os;

}

vector<vector<int>> Creating_PreIncidenceMatrix(vector<vector<int>> IncidenceMatrix){

    vector<vector<int>> PreIncidenceMatrix;

    for(auto& row:IncidenceMatrix){

        vector<int> temp_row_vector;
        for(auto& col:row){
            
            if(col < 0){
                col = abs(col);
            }
            else
                col = 0;

            temp_row_vector.push_back(col);
        }
        PreIncidenceMatrix.push_back(temp_row_vector);
    }

    return PreIncidenceMatrix;
}

void sendEdgesToJava(const pair<int, vector<Edge>>& edges){
    json Edges_json = json::array();

    json json_output;
    //json_output["num_states"] = edges.first; 

    for(const auto& edge : edges.second){
        Edges_json.push_back(edge.to_json());
    }

    cout<<Edges_json.dump()<<endl;
}

vector<vector<int>> Creating_PostIncidenceMatrix(vector<vector<int>> IncidenceMatrix){

    vector<vector<int>> PostIncidenceMatrix;

    for(auto& row:IncidenceMatrix){

        vector<int> temp_row_vector;
        for(auto& col:row){
            
            if(col > 0){
                col = col;
            }
            else
                col = 0;

            temp_row_vector.push_back(col);
        }
        PostIncidenceMatrix.push_back(temp_row_vector);
    }

    return PostIncidenceMatrix;
}

bool Ready_To_Fire_Via_P(vector<vector<int>> PreIncidenceMatrix, int number_of_transition, vector<int> p_marking){

    bool isReady;

    int rows = PreIncidenceMatrix.size();
    int cols = PreIncidenceMatrix[number_of_transition].size();
    
    for(int row = 0; row < rows; row++){
        
        if(PreIncidenceMatrix[row][number_of_transition] > p_marking.at(row)){
            return isReady = false;
        }

    }
    
    return isReady = true;

}

void Print_Matrix(vector<vector<int>> Matrix){
    for(auto& row:Matrix){
        for(auto& col:row){
            cout << col << " ";
        }
        cout << "\n";
    }
}

map<string, vector<int>> Create_preset_t(vector<vector<int>> PreIncidenceMatrix){

    map<string, vector<int>> preset_t;
    string transition_name;
    vector<int> places;
    
    int rows = PreIncidenceMatrix.size();
    int cols = PreIncidenceMatrix[0].size();

    for(int col=0; col < cols; col++){
        places.clear();
        for(int row=0; row < rows; row++){
            if(PreIncidenceMatrix[row][col] != 0){
                places.push_back(row);
            }
        }
        transition_name = to_string(col);
        preset_t[transition_name] = places;
    }

    return preset_t;
}


void PrintMap(const map<string, vector<int>>& preset_t) {
    for (const auto& [key, value] : preset_t) {
        cout << key << ": ";
        for (int num : value) {
            cout << num << " ";
        }
        cout << endl;
    }
}


vector<int> setting_clocks_for_initial_state(vector<int> initial_p_marking, vector<vector<int>> PreIncidenceMatrix){

    vector<int> initial_clocks;

    int cols = PreIncidenceMatrix[0].size();

    for(int i=0; i<cols; i++){
        
        if(Ready_To_Fire_Via_P(PreIncidenceMatrix, i, initial_p_marking)){
            initial_clocks.push_back(0);
        }
        else{
            initial_clocks.push_back(-1);
        }

    }

    return initial_clocks;
}

int defining_k_time(vector<pair<int, int>> interwal_vector, State state, vector<bool> active_transitions){

    vector<int> numbers_to_min_function;

    for(int i = 0; i < active_transitions.size(); i++){
        
        if(active_transitions.at(i) == true){

            pair<int, int> interwal = interwal_vector.at(i);
            int latest_firing_time = interwal.second;

            vector<int> state_t_clock_vector = state.t_clock;
            int current_transition_clock = state_t_clock_vector.at(i);

            int to_time_k = latest_firing_time - current_transition_clock;

            numbers_to_min_function.push_back(to_time_k);

        }
        
    }

    int min = *min_element(begin(numbers_to_min_function), end(numbers_to_min_function));

    return min;

}

bool can_fire_via_time(vector<pair<int, int>> interwal_vector, int which_transition, int current_clock_for_transiton){

    bool can_fire;

    if(current_clock_for_transiton >= interwal_vector.at(which_transition).first && current_clock_for_transiton <= interwal_vector.at(which_transition).second){
        return can_fire = true;
    }
    else{
        return can_fire = false;
    }

}

bool czy_zbior_presets_rozlaczny(map<string, vector<int>> preset_t, int which_transition_fired, int transition_in_loop){

    vector<int> places_for_which_transition_fired = preset_t[to_string(which_transition_fired)];
    vector<int> places_for_transition_in_loop = preset_t[to_string(transition_in_loop)];

    
    bool czy_rozlaczny = true;

    for(int i=0; i<places_for_which_transition_fired.size(); i++){
        
        for(int j=0; j<places_for_transition_in_loop.size(); j++){
            
            if(places_for_which_transition_fired.at(i) == places_for_transition_in_loop.at(j)){

                return czy_rozlaczny = false;

            }
        }


    }

    //cout<<endl<<"Ktora odpalila: "<<which_transition_fired<<" ktora w loop: "<<transition_in_loop<<" czy rozlaczny: "<<czy_rozlaczny<<endl;

    return czy_rozlaczny = true;
    
}

int find_existing_state(const vector<State>& W, const State& new_state){
    for(const State& s : W){
        if (s.p_marking == new_state.p_marking && s.t_clock == new_state.t_clock) {
            return s.state_name; // Found existing state → return its name
        }
    }
    return -1;
}


State Creating_New_State(vector<State> W, State state, vector<vector<int>> PreIncidenceMatrix, vector<vector<int>> PostIncidenceMatrix, int which_transition_fired, map<string, vector<int>> preset_t, int name_var, int czas){

    vector<int> new_p_marking;
    vector<int> new_t_clocks;
    int new_clock_value;
    
    int rows = PreIncidenceMatrix.size();
    int cols = PreIncidenceMatrix[which_transition_fired].size();
    vector<int> tokens_to_add; //nowy marking powstaje poprzez dzialanie: m' = m + (W+ - W-)
    int temp;
    //w przelozeniu na kod: new_p_marking = state.p_marking + (PostIncidenceMatrix[row][which_transition_fired] - PreIncidenceMatrix[row][which_transition_fired])

    //w+ - w-
    for(int row = 0; row < rows; row++){
        
        temp = PostIncidenceMatrix[row][which_transition_fired] - PreIncidenceMatrix[row][which_transition_fired];
        tokens_to_add.push_back(temp);
        //cout<<tokens_to_add.at(row);

    }

    //nowy rozklad tokenow po odpaleniu tranzycji: which_transition_fired;
    for(int i = 0; i<state.p_marking.size(); i++){
        new_p_marking.push_back(state.p_marking.at(i) + tokens_to_add.at(i));

    }


    //do zegarow musi zostac dodany czas ktory minal!!
    //nowe zegary
    for(int i = 0; i<state.t_clock.size(); i++){

        bool can_fire_via_new_p = Ready_To_Fire_Via_P(PreIncidenceMatrix, i, new_p_marking);
        bool can_fire_via_old_p = Ready_To_Fire_Via_P(PreIncidenceMatrix, i, state.p_marking);
        bool czy_preset_rozlaczny = czy_zbior_presets_rozlaczny(preset_t, which_transition_fired, i);

        if(can_fire_via_new_p == false){
            new_t_clocks.push_back(-1);
        }
        else if(can_fire_via_old_p == true && can_fire_via_new_p == true && czy_preset_rozlaczny == true && which_transition_fired != i){
            //cout<<endl<<"weszlo tutaj!"<<" ustawiony czas: "<<state.t_clock.at(i)<<endl;
            new_t_clocks.push_back(state.t_clock.at(i) + czas);
        }
        else{
            new_t_clocks.push_back(0);
        }

    }

    //tu trzeba napisac funkcje, ktora bedzie sprawdzac czy taki stan juz istnieje czy nie, i cos co handluje nazwy bo tj nieoplacalne!

    int new_name = name_var;
    bool is_the_same = false;

    //trzeba sprawdzic czy p_marking juz jest w jakims stanie przetworzonym, jezeli tak to powinien miec ta sama nazwe! jezeli nie to powinien miec inna
    
    State new_state(new_name, new_p_marking, new_t_clocks);

    return new_state;
    
}


pair<int, vector<Edge>> Reachability_Graph(State state, vector<vector<int>> PreIncidenceMatrix, vector<pair<int, int>> interwal_vector, vector<vector<int>> PostIncidenceMatrix, map<string, vector<int>> preset_t){

    map<string, State> R; //stany do przetworzenia

    vector<State> W; //stany przetworzone


    int when_to_stop = 0;
   
    string state_name = to_string(state.state_name);
    R[state_name] = state;
    W.push_back(state);
    
    vector<Edge> E; //krawedzie;

    int cols = PreIncidenceMatrix[0].size();
    
    //dopoki zbior R nie jest pusty
   
    auto it = R.begin();
    int name_var = 1; //do ogarniecia nazw

    while(it != R.end()){

        if(when_to_stop > 150){
            pair<int, vector<Edge>> edges(W.size(), E);
            return edges;
        }
        
        const string& key = it->first; //nazwa stanu
        const State& state = it->second; //State

        //cout<<"Analizowany stan: " << state << "\n";

        vector<bool> czy_spelniaja_warunek_aktywacji;
        int czas;
        //W.push_back(state); //wybrany stan do zbioru przetworzonych

        for(int i=0; i<cols; i++){
                
                
            if(Ready_To_Fire_Via_P(PreIncidenceMatrix, i, state.p_marking)){
                czy_spelniaja_warunek_aktywacji.push_back(true);
                
            }
            else{
                czy_spelniaja_warunek_aktywacji.push_back(false);
            }
            //cout<<czy_spelniaja_warunek_aktywacji.at(i) << "\n";

        }

        bool czy_chociaz_jedna_aktywna = false;
        for (int i = 0; i < czy_spelniaja_warunek_aktywacji.size(); i++){

            if(czy_spelniaja_warunek_aktywacji.at(i) == true){
                czy_chociaz_jedna_aktywna = true;
            }
        }
        
        if(czy_chociaz_jedna_aktywna == true){

        czas = defining_k_time(interwal_vector, state, czy_spelniaja_warunek_aktywacji);

        //cout << "Obliczony czas: "<< czas  << "\n"; //czas dziala i jest policzony wiec zajob

        for(int i = 0; i <= czas; i++){
            //cout<<"Obecny czas: "<<i<<"\n";

            for(int j=0; j < cols; j++){
                //cout<< "Obecna tranzycja: "<<"T"<<j+1<<"\n";

                //to nie moze byc i bo to musi byc clock + i
                int current_clock_for_transition_j = state.t_clock.at(j) + i;

                //cout<<"Current clock for transition: "<<current_clock_for_transition_j<<"\n\n";

                bool can_fire_time = can_fire_via_time(interwal_vector, j, current_clock_for_transition_j);
                bool can_fire_p_marking = Ready_To_Fire_Via_P(PreIncidenceMatrix, j, state.p_marking);
                
                //cout << "Can fire time: "<< can_fire_time << " i "<< "Can fire p_marking: "<< can_fire_p_marking << "\n";
                
                if(can_fire_time && can_fire_p_marking){
                    
                    State new_state = Creating_New_State(W, state, PreIncidenceMatrix, PostIncidenceMatrix, j, preset_t, name_var, czas);
                    
                    int existing_name = find_existing_state(W, new_state);

                    if (existing_name != -1){
                        new_state.state_name = existing_name;
                    }else{
                        new_state.state_name = name_var++;
                        W.push_back(new_state);

                        string new_state_name = to_string(new_state.state_name);
                        R[new_state_name] = new_state;
                        

                    }
                    
                    Edge new_edge(state, new_state, i, "t" + to_string(j));
                    E.push_back(new_edge);

                    
                }
            }
        }
    }
                
        
        it = R.erase(it);
        when_to_stop += 1;
            
    }


    pair<int, vector<Edge>> edges(W.size(), E);    
    return edges;
}

int main(){

    bool holmes_on = true;

    if(holmes_on){

        string jsonInput;
        getline(cin, jsonInput);

        vector<vector<int>> IncidenceMatrix;
        vector<pair<int, int>> interwal_vector;
        vector<int> initial_p_marking;

        try {

            json data = json::parse(jsonInput);

            for(const auto& row : data["incidenceMatrix"]){
                vector<int> matrixRow;
                for(const auto& val : row){
                    matrixRow.push_back(val.get<int>());
                }
                IncidenceMatrix.push_back(matrixRow);
            }

            for(const auto& interval : data["timeIntervals"]){
                int eft = interval[0].get<int>();
                int lft = interval[1].get<int>();
                pair<int, int> single_interval(eft, lft);
                interwal_vector.push_back(single_interval);
            }

            for (const auto& token : data["pmarking"]){
                initial_p_marking.push_back(token.get<int>());
            }

            

            
        } catch(exception& e){
            cerr << "Error while processing JSON: "<<e.what() << "\n";
            return 1;
        }

        vector<vector<int>> PreIncidenceMatrix = Creating_PreIncidenceMatrix(IncidenceMatrix);

        vector<int> initial_state_clocks(setting_clocks_for_initial_state(initial_p_marking, PreIncidenceMatrix));

        State initial_state(0, initial_p_marking, initial_state_clocks);

        map<string, vector<int>> preset_t = Create_preset_t(PreIncidenceMatrix);

        vector<vector<int>> PostIncidenceMatrix = Creating_PostIncidenceMatrix(IncidenceMatrix);
        
        pair<int, vector<Edge>> krawedzie = Reachability_Graph(initial_state, PreIncidenceMatrix, interwal_vector, PostIncidenceMatrix, preset_t);
        
        sendEdgesToJava(krawedzie);

        
    }
    else{

        vector<vector<int>> IncidenceMatrix;
        vector<pair<int, int>> interwal_vector;
        vector<int> initial_p_marking;

        IncidenceMatrix = {
            {-1, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, -1, -1, 7, 0, -100, -1, 0, 0, 0, 0, 0, 3, 0, 100, 0},
            {0, 0, 1, 0, -1, -1, 0, 0, -1, 0, 0, -1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 3, -1, 0, 0, -2, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, -1, -2, 0, 0, 0, 0, 2, 0, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -250, -1, 0, 0, 0, 250},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, -1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1}
        };

        initial_p_marking = {0, 0, 0, 0, 0, 0, 0, 0};
        
        pair<int, int> t0(2, 6);
        pair<int, int> t1(1, 2);
        pair<int, int> t2(2, 6);
        pair<int, int> t3(2, 10);
        pair<int, int> t4(1, 4);
        pair<int, int> t5(2, 5);
        pair<int, int> t6(2, 8);
        pair<int, int> t7(2, 4);
        pair<int, int> t8(1, 4);
        pair<int, int> t9(5, 14);
        pair<int, int> t10(5, 11);
        pair<int, int> t11(1, 2);
        pair<int, int> t12(5, 7);
        pair<int, int> t13(20, 30);
        pair<int, int> t14(1, 2);
        pair<int, int> t15(0, 0);
        pair<int, int> t16(0, 0);
        
        interwal_vector.push_back(t0);
        interwal_vector.push_back(t1);
        interwal_vector.push_back(t2);
        interwal_vector.push_back(t3);
        interwal_vector.push_back(t4);
        interwal_vector.push_back(t5);
        interwal_vector.push_back(t6);
        interwal_vector.push_back(t7);
        interwal_vector.push_back(t8);
        interwal_vector.push_back(t9);
        interwal_vector.push_back(t10);
        interwal_vector.push_back(t11);
        interwal_vector.push_back(t12);
        interwal_vector.push_back(t13);
        interwal_vector.push_back(t14);
        interwal_vector.push_back(t15);
        interwal_vector.push_back(t16);
        
        
       

        vector<vector<int>> PreIncidenceMatrix = Creating_PreIncidenceMatrix(IncidenceMatrix);

        vector<int> initial_state_clocks(setting_clocks_for_initial_state(initial_p_marking, PreIncidenceMatrix));

        State initial_state(0, initial_p_marking, initial_state_clocks);
        map<string, vector<int>> preset_t = Create_preset_t(PreIncidenceMatrix);

        vector<vector<int>> PostIncidenceMatrix = Creating_PostIncidenceMatrix(IncidenceMatrix);

        pair<int, vector<Edge>> krawedzie = Reachability_Graph(initial_state, PreIncidenceMatrix, interwal_vector, PostIncidenceMatrix, preset_t);
        
        //sendEdgesToJava(krawedzie);

        for(int i=0; i<krawedzie.second.size(); i++){
            cout<<krawedzie.second.at(i);
        }

        sendEdgesToJava(krawedzie);





    }
    
    

    return 0;
}
