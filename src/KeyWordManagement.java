
import twitter4j.Status;

public class KeyWordManagement {
	public String[] finance = {"Goldman", "Citi",  "JPMorgan", "Bloomberg", "Morgan Stanley", "BOA", "McKinsey", "Accenture", "Mercer", "KPMG"};
	public String[] sports = {"football", "basketball", "hockey", "sports", "swim", "cr7", "fighting", "Beckham", "Knicks", "Yankees", "Giants", "Patriots"};
	public String[] entertainment = {"Taylor Swift", "Anne Hathaway", "Blake Lively", "Babrielle Anwar", "Natalie Portman", "Usher", "Bruno Mars", "Drake", "Justin Bieber", "David Guetta", "Adele", "Beyonce"};
	public String[] technology = {"Google", "Facebook", "Apple", "Iphone", "Oracle", "IBM", "Dell", "Microsoft", "Amazon", "Software", "hardware", "Cloud", "Big Data", "technology"};
	public String[] keyWords = {"Goldman", "Citi", "JPMorgan", " Bloomberg", "Morgan Stanley", "BOA", "McKinsey", "Accenture", "Mercer", "KPMG",
			"football", "basketball", "hockey", "sports", "swim", "cr7", "boxing", "Beckham", "Knicks", "Yankees", "Giants", "Patriots",
			"Taylor Swift", "Anne Hathaway", "Blake Lively", "Babrielle Anwar", "Natalie Portman", "Usher", "Bruno Mars", "Drake", "Justin Bieber", "David Guetta", "Adele", "Beyonce",
			"Google", "Facebook", "Apple", "Iphone", "Oracle", "IBM", "Dell", "Microsoft", "Amazon", "Software", "hardware", "Cloud", "Big Data", "technology"};
	
	public String getTopic(Status status) {
		String topics = null;
		for (String str : finance) {
			if (status.getText().toLowerCase().contains(str.toLowerCase())) {
				topics = "finance";
				break;
			}
		}
		for (String str : sports) {
			if (status.getText().toLowerCase().contains(str.toLowerCase())) {
				topics = "sports";
				break;
			}
		}
		for (String str : entertainment) {
			if (status.getText().toLowerCase().contains(str.toLowerCase())) {
				topics = "entertainment";
				break;
			}
		}
		for (String str : technology) {
			if (status.getText().toLowerCase().contains(str.toLowerCase())) {
				topics = "technology";
				break;
			}
		}
		return topics;
	}
}