import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

type Gender = "MALE" | "FEMALE" | "UNSELECTED";; // 선택 전 상태 허용

interface Props {
  gender: Gender;
  onChange: (value: Gender) => void;
}

export default function GenderSelector({ gender, onChange }: Props) {
  return (
    <div className="mb-4">
      <label className="block font-semibold mb-1">성별</label>
      <Select value={gender} onValueChange={(value) => onChange(value as Gender)}>
        <SelectTrigger className="w-full">
          <SelectValue placeholder="선택" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="UNSELECTED">선택</SelectItem>
          <SelectItem value="MALE">남성</SelectItem>
          <SelectItem value="FEMALE">여성</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
}